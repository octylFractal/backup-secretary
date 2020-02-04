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

package net.octyl.backup.local.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.mapNotNull
import net.octyl.backup.config.ConfigNode
import net.octyl.backup.config.Configurable
import net.octyl.backup.config.configKey
import net.octyl.backup.config.set
import net.octyl.backup.core.collection.setFrom
import net.octyl.backup.core.plugin.PluginId
import net.octyl.backup.core.plugin.SimplePluginCompanion
import net.octyl.backup.core.plugin.pluginId
import net.octyl.backup.core.source.StorageSource
import net.octyl.backup.core.source.buildPathMatcher
import net.octyl.backup.core.status.StatusLevel
import net.octyl.backup.core.status.requireStatusReporter
import net.octyl.backup.util.flow.FlowFiles
import java.nio.file.Files
import java.nio.file.Path
import java.util.TreeSet

class LocalStorageSource : StorageSource, Configurable {

    companion object : SimplePluginCompanion<StorageSource> {
        private val SOURCE_FILES = configKey<List<String>>("sourceFiles")
        private val EXCLUDES = configKey<List<String>>("excludes")

        override val id: PluginId<StorageSource> = pluginId("net.octyl.local")
        override val provider = ::LocalStorageSource
    }

    override val id = Companion.id

    private val sourceFilesMut = TreeSet<String>()
    private val excludesMut = TreeSet<String>()

    var sourceFiles: Set<String>
        get() = sourceFilesMut
        set(value) = sourceFilesMut.setFrom(value)

    var excludes: Set<String>
        get() = excludesMut
        set(value) = excludesMut.setFrom(value)

    override fun loadConfigurationFrom(configNode: ConfigNode) {
        sourceFilesMut.setFrom(configNode, SOURCE_FILES)
        excludesMut.setFrom(configNode, EXCLUDES)
    }

    override fun saveConfigurationTo(configNode: ConfigNode) {
        configNode[SOURCE_FILES] = sourceFilesMut.toList()
        configNode[EXCLUDES] = excludesMut.toList()
    }

    override fun provideFiles(): Flow<Path> {
        val pathMatcher = buildPathMatcher(excludesMut)
        return sourceFilesMut.asFlow()
            .mapNotNull { sourceFile ->
                val result = Path.of(sourceFile).takeIf { Files.exists(it) }
                if (result == null) {
                    requireStatusReporter().report(
                        StatusLevel.WARN, "Source path is missing: '$sourceFile'"
                    )
                }
                result
            }
            .flatMapConcat { source ->
                FlowFiles.walk(source)
            }
            .filterNot {
                pathMatcher.matches(it)
            }
    }
}
