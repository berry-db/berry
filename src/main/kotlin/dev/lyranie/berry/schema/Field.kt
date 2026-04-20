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
