/*
 * This file is part of backup-secretary, licensed under the MIT License (MIT).
 *
 * Copyright (c) Octavia Togami <https://octyl.net>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.octyl.backup.core.plugin

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSetMultimap
import java.util.ServiceLoader
import kotlin.reflect.KClass
import kotlin.streams.toList

interface PluginProvider {

    /**
     * The set of plugin IDs served by this provider.
     */
    val providedIds: Set<PluginId<*>>

    fun <I : Plugin<I>> providePlugin(id: PluginId<I>): I

}

inline fun <reified I : Plugin<I>> PluginProvider.providePlugin(key: String) =
    providePlugin(pluginId<I>(key))

private val providerList = ServiceLoader.load(PluginProvider::class.java).stream()
    .map { it.get() }.toList()

/**
 * A Map of providers for a given plugin type.
 */
val providers: ImmutableSetMultimap<KClass<*>, PluginProvider> by lazy {
    ImmutableSetMultimap.builder<KClass<*>, PluginProvider>().also { builder ->
        providerList.forEach { provider ->
            provider.providedIds.asSequence()
                .map { id -> id.type }
                .distinct()
                .forEach { type -> builder.put(type, provider) }
        }
    }.build()
}

/**
 * A map of providers for a given plugin ID.
 */
val providersById: Map<PluginId<*>, PluginProvider> by lazy {
    val map = mutableMapOf<PluginId<*>, PluginProvider>()
    for (provider in providerList) {
        for (id in provider.providedIds) {
            val existing = map[id]
            check(existing == null) {
                "Provider '${provider.javaClass.name}'" +
                    " tried to override plugin ID '$id'" +
                    " from provider '${existing!!.javaClass.name}'"
            }
            map[id] = provider
        }
    }
    ImmutableMap.copyOf(map)
}

inline fun <reified I : Plugin<I>> findProvidersFor(): Set<PluginProvider> {
    return providers[I::class]
}

inline fun <reified I : Plugin<I>> findProvider(key: String): PluginProvider? {
    return providersById[pluginId<I>(key)]
}

inline fun <reified I : Plugin<I>> findPlugin(key: String): I? {
    return findProvider<I>(key)?.providePlugin<I>(key)
}
