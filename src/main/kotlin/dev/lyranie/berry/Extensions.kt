package dev.lyranie.berry

import java.io.RandomAccessFile

internal fun byteArrayOf(vararg elements: Char) = elements.map { it.code.toByte() }.toByteArray()

fun RandomAccessFile.readNBytes(length: Int): ByteArray {
    val bytes = ByteArray(length)
    for (i in 0 until length) {
        bytes[i] = readByte()
    }

    return bytes
}
