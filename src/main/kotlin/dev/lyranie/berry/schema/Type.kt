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
