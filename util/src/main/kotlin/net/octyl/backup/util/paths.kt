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

import com.google.common.net.PercentEscaper
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


// retains / for paths, a few common chars. do not convert spaces.
private val SAFE_PATH_ESCAPER = PercentEscaper("/-_.", false)

/**
 * Convert the given path to a safe path, compatible with all OSes, but retain
 * uniqueness among all paths.
 */
fun encodeSafePath(path: String): String {
    return SAFE_PATH_ESCAPER.escape(path)
}

/**
 * Undo the effects of [encodeSafePath].
 */
fun decodeSafePath(path: String): String {
    return URLDecoder.decode(path, StandardCharsets.UTF_8)
}
