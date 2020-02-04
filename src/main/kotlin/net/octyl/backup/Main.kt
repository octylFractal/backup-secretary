package net.octyl.backup

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

fun main(args: Array<String>) {
    val file = args.elementAtOrNull(0) ?: throw IllegalArgumentException("Usage: zcs <file>")
    ZipFile(file).use { zf ->
        val maxMethodSize = zf.maxFieldSize { methodStr }
        val maxNameSize = zf.maxFieldSize { name }
        val maxCmpSize = zf.maxFieldSize { compressedSize }
        for (entry in zf.entries().asSequence().sortedByDescending { it.compressedSize }) {
            println(entry.name.padStart(maxNameSize) +
                " " + entry.methodStr.padStart(maxMethodSize) +
                " " + entry.compressedSize.toString().padStart(maxCmpSize))
        }
    }
}

private inline fun ZipFile.maxFieldSize(crossinline block: ZipEntry.() -> Any): Int {
    return entries().asSequence().map { it.block().toString().length }
        .max() ?: 0
}

private val ZipEntry.methodStr
    get() = when (method) {
        ZipEntry.DEFLATED -> "DEFLATED"
        ZipEntry.STORED -> "STORED"
        else -> "UNKNOWN"
    }
