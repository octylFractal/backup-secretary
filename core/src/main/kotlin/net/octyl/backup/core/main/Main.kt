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

package net.octyl.backup.core.main

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.octyl.backup.config.openConfig
import net.octyl.backup.util.flow.FlowFiles
import net.octyl.backup.util.flow.tickerFlow
import net.octyl.backup.core.setup.BackupSetup
import net.octyl.backup.core.setup.BackupSetupStorage
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.ZonedDateTime

fun main() {
    val dataDirectory = Path.of("./data")
    val setups = dataDirectory.resolve("setups")
    Files.createDirectories(setups)
    val component = DaggerMainComponent.create()

    runBlocking(Dispatchers.Default) {
        val setupStorage = component.setupStorage
        loadSetups(setups, setupStorage)
        launch {
            tickerFlow(Duration.ofSeconds(1))
                .collect { runReadyBackups(setupStorage, component) }
        }
    }
}

suspend fun loadSetups(setups: Path, setupStorage: BackupSetupStorage) {
    FlowFiles.list(setups).collect {
        val config = openConfig(it)
        val setup = BackupSetup(config)
        setupStorage.store(it.fileName.toString().substringBeforeLast('.'), setup)
    }
}

private suspend fun runReadyBackups(setupStorage: BackupSetupStorage, component: MainComponent) {
    setupStorage.listReadySetups()
        .collect { (key, setup) ->
            val backupComponent = component.backupComponentBuilder()
                .bindBackupSetup(setup)
                .build()
            try {
                backupComponent.backupTask.run()
            } finally {
                setup.scheduledTime?.let { time ->
                    val next = ZonedDateTime.now().plusDays(1).with(time)
                    setupStorage.store(key, setup.copy(nextBackupTime = next.toInstant()))
                }
            }
        }
}
