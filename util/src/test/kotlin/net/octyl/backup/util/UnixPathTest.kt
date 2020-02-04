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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("A UnixPath created from")
class UnixPathTest {
    @DisplayName("an empty string")
    @Nested
    inner class EmptyString {

        private lateinit var emptyPath: UnixPath

        @BeforeEach
        fun initializePath() {
            emptyPath = unixPath("")
        }

        @Test
        @DisplayName("has one empty part")
        fun hasOneEmptyPart() {
            assertEquals(1, emptyPath.parts.size)
            assertEquals("", emptyPath.part(0))
        }

        @Test
        @DisplayName("returns the empty string from toString()")
        fun returnsEmptyStringFromToString() {
            assertEquals("", emptyPath.toString())
        }

        @Test
        @DisplayName("is not absolute")
        fun isNotAbsolute() {
            assertFalse(emptyPath.isAbsolute)
        }

        @Test
        @DisplayName("is empty")
        fun isEmpty() {
            assertTrue(emptyPath.isEmpty)
        }

        @Test
        @DisplayName("toAbsolutePath() returns /")
        fun toAbsolutePathReturnsSlash() {
            assertEquals("/", emptyPath.toAbsolutePath().toString())
        }

        @Test
        @DisplayName("other paths resolved against this return other path")
        fun otherPathsResolveToOther() {
            assertEquals(unixPath("/"), emptyPath.resolve("/"))
            assertEquals(unixPath("foo"), emptyPath.resolve("foo"))
            assertEquals(unixPath(""), emptyPath.resolve(""))
        }

        @Test
        @DisplayName("resolving this path against others returns the other path")
        fun thisPathResolvesToOthers() {
            assertEquals(unixPath("/"), unixPath("/").resolve(emptyPath))
            assertEquals(unixPath("foo"), unixPath("foo").resolve(emptyPath))
            assertEquals(unixPath(""), unixPath("").resolve(emptyPath))
        }

        @Test
        @DisplayName("is equal as another instance")
        fun isEqualToItself() {
            assertEquals(unixPath(""), emptyPath)
        }

        @Test
        @DisplayName("has the same hashCode as another equal instance")
        fun hasSameHashCodeAsItself() {
            assertEquals(unixPath("").hashCode(), emptyPath.hashCode())
        }
    }

    @DisplayName("the string \"/\"")
    @Nested
    inner class SlashString {

        private lateinit var absolutePath: UnixPath

        @BeforeEach
        fun initializePath() {
            absolutePath = unixPath("/")
        }

        @Test
        @DisplayName("has one empty part")
        fun hasOneEmptyPart() {
            assertEquals(1, absolutePath.parts.size)
            assertEquals("", absolutePath.part(0))
        }

        @Test
        @DisplayName("returns \"/\" string from toString()")
        fun returnsSlashStringFromToString() {
            assertEquals("/", absolutePath.toString())
        }

        @Test
        @DisplayName("is absolute")
        fun isAbsolute() {
            assertTrue(absolutePath.isAbsolute)
        }

        @Test
        @DisplayName("is not empty")
        fun isEmpty() {
            assertFalse(absolutePath.isEmpty)
        }

        @Test
        @DisplayName("toAbsolutePath() returns /")
        fun toAbsolutePathReturnsSlash() {
            assertEquals("/", absolutePath.toAbsolutePath().toString())
        }

        @Test
        @DisplayName("other paths resolved against this return this path + other")
        fun otherPathsResolveToOther() {
            assertEquals(unixPath("/"), absolutePath.resolve("/"))
            assertEquals(unixPath("/foo"), absolutePath.resolve("foo"))
            assertEquals(unixPath("/"), absolutePath.resolve(""))
        }

        @Test
        @DisplayName("resolving this path against others returns this path")
        fun thisPathResolvesToOthers() {
            assertEquals(absolutePath, unixPath("/").resolve(absolutePath))
            assertEquals(absolutePath, unixPath("foo").resolve(absolutePath))
            assertEquals(absolutePath, unixPath("").resolve(absolutePath))
        }

        @Test
        @DisplayName("is equal as another instance")
        fun isEqualToItself() {
            assertEquals(unixPath("/"), absolutePath)
        }

        @Test
        @DisplayName("has the same hashCode as another equal instance")
        fun hasSameHashCodeAsItself() {
            assertEquals(unixPath("/").hashCode(), absolutePath.hashCode())
        }
    }

}
