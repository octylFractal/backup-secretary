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

import java.util.Objects

internal class UnixPathImpl(
    override val isAbsolute: Boolean,
    private val partsArray: Array<String>
) : UnixPath {

    private val firstPartIsEmpty: Boolean = partsArray.first().isEmpty()
    override val isEmpty = !isAbsolute && firstPartIsEmpty

    override val parts = partsArray.asList()

    override fun part(index: Int): String {
        require(index in partsArray.indices) { "Index must be between 0 and ${partsArray.lastIndex}" }
        return partsArray[index]
    }

    override fun toAbsolutePath(): UnixPath {
        if (isAbsolute) {
            return this
        }

        val partList = ArrayList<String>(partsArray.size)
        var doubleDots = 0
        // iterate in reverse order for easy .. handling
        loop@ for (i in partsArray.indices.reversed()) {
            when (val part = partsArray[i]) {
                ".." -> {
                    doubleDots++
                    continue@loop
                }
                "." -> {
                    continue@loop
                }
                else -> {
                    if (doubleDots > 0) {
                        doubleDots--
                        continue@loop
                    }
                    partList.add(part)
                }
            }
        }
        // excess doubleDots are ok, they just loop back to the root like in Unix

        // reverse partList again
        val newParts = Array(partList.size) { index -> partList[partList.size - index - 1] }
        return UnixPathImpl(true, newParts)
    }

    override fun resolve(path: UnixPath): UnixPath {
        if (path.isAbsolute) {
            return path
        }
        if (path.isEmpty) {
            return this
        }
        if (this.isEmpty) {
            return path
        }
        val newParts = when {
            firstPartIsEmpty -> path.parts.toTypedArray()
            else -> partsArray + path.parts
        }
        return UnixPathImpl(isAbsolute, newParts)
    }

    override fun toString(): String {
        val capacity = (if (isAbsolute) 1 else 0) + partsArray.sumBy { it.length }
        return partsArray.joinTo(StringBuilder(capacity), prefix = if (isAbsolute) "/" else "", separator = "/")
            .toString()
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is UnixPath -> isAbsolute == other.isAbsolute && parts == other.parts
            else -> false
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(isAbsolute, parts)
    }
}
