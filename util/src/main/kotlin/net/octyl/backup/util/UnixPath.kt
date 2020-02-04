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

package net.octyl.backup.util

import java.nio.file.Path

/**
 * Simplified path interface supporting only Unix-style paths.
 *
 * The default directory is `/`, so `foo` and `/foo` are the same.
 */
interface UnixPath {

    val isAbsolute: Boolean

    val isEmpty: Boolean

    fun part(index: Int): String

    val parts: List<String>

    fun toAbsolutePath(): UnixPath

    fun resolve(path: UnixPath): UnixPath

    fun resolve(vararg path: String): UnixPath = resolve(unixPath(*path))

    operator fun plus(path: UnixPath) = resolve(path)

    operator fun plus(path: String) = resolve(path)

    override fun toString(): String

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

}

/**
 * Create Unix path with trusted parts array.
 */
private fun unixPathInternal(isAbsolute: Boolean, parts: Array<String>): UnixPath {
    check(parts.none { it.contains("/") }) { "Path parts may not contain a slash" }
    return UnixPathImpl(isAbsolute, parts)
}

private fun unixPathNormalized(parts: Array<String>): UnixPath {
    require(parts.isNotEmpty()) { "Must have at least one part" }
    val isAbsolute = parts.first().startsWith('/')
    if (isAbsolute) {
        parts[0] = parts.first().substring(1)
    }
    val normParts = parts.asSequence()
        .flatMap { it.splitToSequence("/") }
        .onEach { value ->
            check(value.isNotEmpty() || parts.size == 1) { "A path part may only be empty if it is the only part" }
            check(value.none { ch -> ch.isISOControl() || ch == '\u0000' }) {
                "Invalid characters in $value"
            }
        }
        .toList()
        .toTypedArray()
    return unixPathInternal(isAbsolute, normParts)
}

fun unixPath(vararg parts: String) = unixPathNormalized(arrayOf(*parts))

fun unixPath(parts: Collection<String>) = unixPathNormalized(parts.toTypedArray())

fun Path.toUnixPath() = unixPath(sequence {
    if (isAbsolute) {
        yield("/")
    }
    for (p in this@toUnixPath) {
        yield(p.toString())
    }
}.toList())
