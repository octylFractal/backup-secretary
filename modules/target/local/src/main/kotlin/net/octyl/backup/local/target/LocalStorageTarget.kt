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

package net.octyl.backup.local.target

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.octyl.backup.config.ConfigNode
import net.octyl.backup.config.Configurable
import net.octyl.backup.config.configKey
import net.octyl.backup.config.get
import net.octyl.backup.config.set
import net.octyl.backup.core.chunk.Chunk
import net.octyl.backup.core.chunk.SimpleChunk
import net.octyl.backup.core.plugin.PluginId
import net.octyl.backup.core.plugin.SimplePluginCompanion
import net.octyl.backup.core.plugin.pluginId
import net.octyl.backup.core.target.StorageTarget
import net.octyl.backup.util.UnixPath
import net.octyl.backup.util.decodeSafePath
import net.octyl.backup.util.encodeSafePath
import net.octyl.backup.util.flow.FlowFiles
import net.octyl.backup.util.okio.LazySource
import net.octyl.backup.util.unixPath
import okio.buffer
import okio.sink
import okio.source
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path

class LocalStorageTarget : StorageTarget, Configurable {
    companion object : SimplePluginCompanion<StorageTarget> {
        private val STORAGE_FOLDER = configKey<String>("storageFolder")

        override val id: PluginId<StorageTarget> = pluginId("net.octyl.local")
        override val provider = ::LocalStorageTarget
    }

    private var storageFolder: Path = Path.of("")

    override val id = Companion.id

    override fun loadConfigurationFrom(configNode: ConfigNode) {
        configNode[STORAGE_FOLDER]?.let {
            storageFolder = Path.of(it)
        }
    }

    override fun saveConfigurationTo(configNode: ConfigNode) {
        configNode[STORAGE_FOLDER] = storageFolder.toString()
    }

    private suspend fun UnixPath.toStoragePath(): Path {
        val safeUnixPath = encodeSafePath(toString())
        val realPath = withContext(Dispatchers.IO) {
            storageFolder.resolve(safeUnixPath).toRealPath(LinkOption.NOFOLLOW_LINKS)
        }
        require(realPath.startsWith(storageFolder)) {
            "Invalid path: $this"
        }
        return realPath
    }

    override suspend fun retrieve(key: UnixPath): Chunk? {
        val realPath = key.toStoragePath()
        val isRegularFile = withContext(Dispatchers.IO) { Files.isRegularFile(realPath) }
        if (!isRegularFile) {
            return null
        }
        return SimpleChunk(key, LazySource { Files.newInputStream(realPath).source() })

    }

    private fun decodeUnixPath(path: Path): UnixPath {
        val relativePath = storageFolder.relativize(path).toString()
        val decodedRelPath = decodeSafePath(relativePath)
        return unixPath(decodedRelPath)
    }

    override fun list(): Flow<UnixPath> =
        FlowFiles.walk(storageFolder)
            .map { decodeUnixPath(it) }

    override fun list(prefix: UnixPath): Flow<UnixPath> {
        if (prefix.isEmpty) {
            // this is just listing the root folder
            return list()
        }
        // run this outside of flow to provide logical stack traces
        val parent = runBlocking(Dispatchers.IO) { prefix.toStoragePath().parent }
        return flow {
            if (!Files.isDirectory(parent)) {
                // we have nothing to list then :)
                return@flow
            }
            emitAll(FlowFiles.walk(parent).map { decodeUnixPath(it) })
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun store(chunk: Chunk) {
        require(!chunk.path.isEmpty) { "Chunk path may not be empty" }
        val realPath = chunk.path.toStoragePath()
        withContext(Dispatchers.IO) {
            // Create any directories needed (may do a harmless attempt to recreate storage dir)
            Files.createDirectories(realPath.parent)
            // Then copy the source data to the destination
            Files.newOutputStream(realPath).sink().use {
                chunk.source.buffer().readAll(it)
            }
        }
    }
}
