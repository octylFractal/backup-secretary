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

import net.octyl.backup.config.ConfigKey
import net.octyl.backup.config.ConfigNode
import net.octyl.backup.config.Configurable
import net.octyl.backup.config.require

/**
 * Represents an item that can be plugged in to the core systems.
 *
 * @param I the interface this plugin represents
 */
interface Plugin<I : Plugin<I>> {

    /**
     * Unique ID of this plugin.
     */
    val id: PluginId<I>

}

inline fun <reified I : Plugin<I>> ConfigNode.providePlugin(key: String): I =
    require<String>(key)
        .let { requireNotNull(findPlugin<I>(it)) { "No plugin registered with id '$it'" } }
        .also { plugin ->
            (plugin as? Configurable)?.loadConfigurationFrom(child(key))
        }

inline fun <reified I : Plugin<I>> ConfigNode.providePlugin(configKey: ConfigKey<String>): I =
    providePlugin(configKey.key)
