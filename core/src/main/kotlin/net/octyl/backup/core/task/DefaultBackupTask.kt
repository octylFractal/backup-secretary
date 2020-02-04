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

package net.octyl.backup.core.task

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import net.octyl.backup.core.setup.BackupSetup
import net.octyl.backup.core.status.ReporterContext
import net.octyl.backup.core.status.StatusLevel
import net.octyl.backup.core.status.StatusReporter
import javax.inject.Inject

class DefaultBackupTask @Inject constructor(
    private val statusReporter: StatusReporter,
    private val setup: BackupSetup
) : BackupTask {
    override suspend fun run() {
        withContext(ReporterContext(statusReporter)) {
            val files = setup.storageSource.provideFiles()
                .onEach { file ->
                    statusReporter.report(
                        StatusLevel.INFO,
                        "Processing file: '$file'"
                    )
                }
            val chunks = setup.fileChunker.chunk(files, setup.storageTarget)
            // store the chunks async, to offer maximum file I/O
            coroutineScope {
                chunks
                    .map {
                        async {
                            setup.storageTarget.store(it)
                        }
                    }
                    // up to ~40 concurrent store tasks
                    .buffer(40)
                    .collect {
                        it.await()
                    }
            }
        }
    }
}
