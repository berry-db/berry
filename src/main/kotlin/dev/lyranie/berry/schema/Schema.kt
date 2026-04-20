package dev.lyranie.berry.schema

import dev.lyranie.berry.parser.schema.Schema
import dev.lyranie.berry.parser.schema.Table
import dev.lyranie.commons.reader
import dev.lyranie.commons.writer

fun Schema.writeData() {
    writer {
        writeUTF(name)
        writeInt(tables.size)
        tables.forEach { it.writeData() }
    }
}

fun Schema.Companion.readData(): Schema = reader {
    val name = readUTF()
    val tablesSize = readInt()
    val tables = mutableListOf<Table>()

    for (i in 0 until tablesSize) {
        tables.add(Table.readData())
    }

    Schema(name, tables)
}
