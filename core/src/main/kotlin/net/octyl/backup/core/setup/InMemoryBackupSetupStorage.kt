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

import com.google.common.collect.Multimaps
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.util.NavigableMap
import java.util.TreeMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryBackupSetupStorage @Inject constructor() : BackupSetupStorage {

    private val mutex = Mutex()
    private val map = mutableMapOf<String, BackupSetup>()
    private val priorityMap = Multimaps.newSetMultimap(
        TreeMap<Instant, Collection<Pair<String, BackupSetup>>>()
    ) { linkedSetOf() }

    override suspend fun store(key: String, setup: BackupSetup) {
        mutex.withLock {
            map.put(key, setup)?.let { old ->
                priorityMap.remove(old.nextBackupTime, key to old)
            }
            priorityMap.put(setup.nextBackupTime, key to setup)
        }
    }

    override suspend fun remove(key: String) {
        mutex.withLock {
            map.remove(key)?.let { priorityMap.remove(it.nextBackupTime, key to it) }
        }
    }

    override suspend fun retrieve(key: String): BackupSetup? {
        mutex.withLock {
            return map[key]
        }
    }

    override fun list() = flow {
        mutex.withLock { map.keys.toList() }.forEach { emit(it) }
    }

    override fun listReadySetups() = flow {
        mutex.withLock {
            val navMap = priorityMap.asMap() as NavigableMap<Instant, Collection<Pair<String, BackupSetup>>>
            navMap.headMap(Instant.now()).values.toList()
        }.flatten().forEach { emit(it) }
    }
}
