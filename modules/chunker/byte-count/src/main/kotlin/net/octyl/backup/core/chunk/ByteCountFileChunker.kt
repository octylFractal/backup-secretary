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

package net.octyl.backup.core.chunk

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import net.octyl.backup.config.ConfigNode
import net.octyl.backup.config.Configurable
import net.octyl.backup.config.configKey
import net.octyl.backup.config.get
import net.octyl.backup.config.set
import net.octyl.backup.core.plugin.PluginId
import net.octyl.backup.core.plugin.SimplePluginCompanion
import net.octyl.backup.core.plugin.pluginId
import net.octyl.backup.core.status.StatusLevel
import net.octyl.backup.core.status.requireStatusReporter
import net.octyl.backup.core.target.InputStorageTarget
import net.octyl.backup.util.okio.LazySource
import net.octyl.backup.util.toUnixPath
import okio.Buffer
import okio.buffer
import okio.source
import java.nio.file.Files
import java.nio.file.Path

class ByteCountFileChunker : FileChunker, Configurable {

    companion object : SimplePluginCompanion<FileChunker> {
        private val CHUNK_SIZE = configKey<String>("chunkSize")

        override val id: PluginId<FileChunker> = pluginId("net.octyl.bytecount")
        override val provider = ::ByteCountFileChunker
    }

    override val id = Companion.id

    private var chunkSize: ChunkSize = ChunkSize.Bytes(1024 * 1024 * 1024) // default: 1MB

    override fun loadConfigurationFrom(configNode: ConfigNode) {
        configNode[CHUNK_SIZE]?.let {
            chunkSize = ChunkSize.fromString(it)
        }
    }

    override fun saveConfigurationTo(configNode: ConfigNode) {
        configNode[CHUNK_SIZE] = chunkSize.toString()
    }

    override suspend fun chunk(files: Flow<Path>, target: InputStorageTarget): Flow<Chunk> {
        return files.flatMapConcat {
            chunkSize
                .chunkFile(it)
                .catch { error ->
                    requireStatusReporter().report(
                        StatusLevel.ERROR,
                        "Error processing file '$it'",
                        error
                    )
                    // re-throw if fatal
                    if (error !is Exception) {
                        throw error
                    }
                }
        }
    }
}

private const val CHUNK_SIZE_IS_FILE_SIZE = "file_size"

private sealed class ChunkSize(private val toString: String) {
    companion object {
        fun fromString(string: String): ChunkSize = when (string) {
            CHUNK_SIZE_IS_FILE_SIZE -> File
            else -> Bytes(string.toInt())
        }
    }

    override fun toString() = toString

    abstract fun chunkFile(path: Path): Flow<Chunk>

    object File : ChunkSize(CHUNK_SIZE_IS_FILE_SIZE) {
        override fun chunkFile(path: Path): Flow<Chunk> {
            return flow {
                emit(SimpleChunk(path.toUnixPath(), LazySource { Files.newInputStream(path).source() }))
            }
        }
    }

    data class Bytes(val bytes: Int) : ChunkSize(bytes.toString()) {
        override fun chunkFile(path: Path): Flow<Chunk> {
            return flow<Chunk> {
                // `flowOn` fixes this, see IDEA-223285
                @Suppress("BlockingMethodInNonBlockingContext")
                Files.newInputStream(path).source().buffer().use { source ->
                    val byteCount = bytes.toLong()
                    val buffer = Buffer()
                    var count = 0
                    while (!source.exhausted()) {
                        // ignore true/false, we just want to batch up `bytes` if possible
                        source.request(byteCount)
                        val read = source.read(buffer, byteCount)
                        // due to request + read, we _should_ always read `bytes` unless there are no more
                        check(read == byteCount || source.exhausted()) { "Should have read $byteCount, only got $read" }
                        val chunkPath = path.toUnixPath().resolve("chunk#$count")
                        emit(SimpleChunk(chunkPath, buffer.copy()))
                        count++
                    }
                }
            }.flowOn(Dispatchers.IO)
        }
    }
}
