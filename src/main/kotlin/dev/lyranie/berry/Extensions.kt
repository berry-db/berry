/*
 * Copyright (c) 2026 lyranie
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
