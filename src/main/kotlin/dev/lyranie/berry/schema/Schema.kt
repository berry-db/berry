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
