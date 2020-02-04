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

package net.octyl.backup.core.setup

import net.octyl.backup.config.ConfigNode
import net.octyl.backup.config.configKey
import net.octyl.backup.config.get
import net.octyl.backup.config.require
import net.octyl.backup.config.set
import net.octyl.backup.core.inject.BackupScoped
import net.octyl.backup.core.plugin.providePlugin
import net.octyl.backup.core.source.StorageSource
import net.octyl.backup.core.chunk.FileChunker
import net.octyl.backup.core.target.StorageTarget
import java.time.Instant
import java.time.LocalTime

@BackupScoped
data class BackupSetup(
    val storageSource: StorageSource,
    val fileChunker: FileChunker,
    val storageTarget: StorageTarget,
    val nextBackupTime: Instant,
    /**
     * The time at which this backup is run daily, if configured to do so.
     */
    val scheduledTime: LocalTime? = null
) {
    companion object {
        private val STORAGE_SOURCE = configKey<String>("source")
        private val FILE_CHUNKER = configKey<String>("chunker")
        private val STORAGE_TARGET = configKey<String>("target")
        private val NEXT_BACKUP_TIME = configKey<Instant>("nextBackupTime")
        private val SCHEDULED_TIME = configKey<LocalTime>("scheduleTime")
    }

    constructor(
        configNode: ConfigNode
    ) : this(
        configNode.providePlugin<StorageSource>(STORAGE_SOURCE),
        configNode.providePlugin<FileChunker>(FILE_CHUNKER),
        configNode.providePlugin<StorageTarget>(STORAGE_TARGET),
        configNode.require(NEXT_BACKUP_TIME),
        configNode[SCHEDULED_TIME]
    )

    fun saveConfigurationTo(configNode: ConfigNode) {
        configNode[STORAGE_SOURCE] = storageSource.id.key
        configNode[FILE_CHUNKER] = fileChunker.id.key
        configNode[STORAGE_TARGET] = storageTarget.id.key
        configNode[NEXT_BACKUP_TIME] = nextBackupTime
        scheduledTime?.let {
            configNode[SCHEDULED_TIME] = scheduledTime
        }
    }
}
