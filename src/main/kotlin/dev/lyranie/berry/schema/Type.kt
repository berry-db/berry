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

package dev.lyranie.berry.schema

import dev.lyranie.berry.parser.exception.UnsupportedTypeException
import dev.lyranie.berry.parser.schema.Type
import dev.lyranie.commons.reader
import dev.lyranie.commons.writer

fun Type.writeData() {
    val self = this

    writer {
        writeByte(byte.toInt())
        if (self is Type.Reference) {
            writeUTF(name)
        }
        if (self is Type.List) {
            writeUTF(name)
        }
    }
}

fun Type.Companion.readData(): Type = reader {
    when (val byte = readByte()) {
        0x1.toByte() -> {
            STRING
        }

        0x2.toByte() -> {
            NUMBER
        }

        0x3.toByte() -> {
            BOOLEAN
        }

        0x4.toByte() -> {
            val name = readUTF()
            Type.Reference(name)
        }

        0x5.toByte() -> {
            val name = readUTF()
            Type.List(name)
        }

        else -> {
            throw UnsupportedTypeException(byte::class)
        }
    }
}
