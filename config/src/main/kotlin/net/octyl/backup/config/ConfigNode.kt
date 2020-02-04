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

package net.octyl.backup.config

import kotlin.reflect.KClass

interface ConfigNode {

    fun <T : Any> get(key: String, type: KClass<T>): T? = get(listOf(key), type)

    fun <T : Any> get(key: List<String>, type: KClass<T>): T?

    fun <T : Any> require(key: String, type: KClass<T>): T = require(listOf(key), type)

    fun <T : Any> require(key: List<String>, type: KClass<T>): T = requireNotNull(get(key, type)) {
        "Missing required key '$key'"
    }

    operator fun set(key: String, value: Any) = set(listOf(key), value)

    operator fun set(key: List<String>, value: Any)

    fun save()

    fun child(key: String): ConfigNode

}

inline operator fun <reified T : Any> ConfigNode.get(key: String) = get(key, T::class)
inline fun <reified T : Any> ConfigNode.require(key: String) = require(key, T::class)
