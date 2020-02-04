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

package net.octyl.backup.core.target

import kotlinx.coroutines.flow.Flow
import net.octyl.backup.core.chunk.Chunk
import net.octyl.backup.util.UnixPath

/**
 * A simplified input file-system interface.
 */
interface InputStorageTarget {

    /**
     * Retrieve content stored under [key].
     */
    suspend fun retrieve(key: UnixPath): Chunk?

    /**
     * List all keys.
     *
     * Listing does not occur until the flow is consumed.
     */
    fun list(): Flow<UnixPath>

    /**
     * List prefixed keys. This includes partial file name matches, so
     * `foo/b` may have `foo/bar.txt` returned.
     *
     * Listing does not occur until the flow is consumed.
     */
    fun list(prefix: UnixPath): Flow<UnixPath>

}
