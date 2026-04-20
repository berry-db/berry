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

import dev.lyranie.berry.parser.schema.Field
import dev.lyranie.berry.parser.schema.Parameter
import dev.lyranie.berry.parser.schema.Type
import dev.lyranie.commons.reader
import dev.lyranie.commons.writer

fun Field.writeData() {
    writer {
        writeUTF(name)
        type.writeData()
        writeInt(parameters.size)
        parameters.forEach(Parameter::writeData)
    }
}

fun Field.Companion.readData(): Field = reader {
    val name = readUTF()
    val type = Type.readData()
    val parameters = readParameters()

    Field(name, type, parameters)
}

private fun Field.Companion.readParameters(): List<Parameter> = reader {
    val size = readInt()
    val parameters = arrayListOf<Parameter>()

    for (i in 0 until size) {
        parameters.add(Parameter.readData())
    }

    parameters
}

fun Field.isPrimary(): Boolean = parameters.contains(Parameter.Primary)
