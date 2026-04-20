package dev.lyranie.berry.schema

import dev.lyranie.berry.internal.exception.MalformedDatabaseException
import dev.lyranie.berry.parser.schema.Parameter
import dev.lyranie.commons.reader
import dev.lyranie.commons.writer

fun Parameter.Companion.readData(): Parameter = reader {
    when (readByte()) {
        0x1.toByte() -> Parameter.Primary
        0x2.toByte() -> Parameter.Unique
        else -> throw MalformedDatabaseException()
    }
}

fun Parameter.writeData() {
    writer {
        writeByte(byte.toInt())
    }
}
