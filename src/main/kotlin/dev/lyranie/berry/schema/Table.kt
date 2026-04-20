package dev.lyranie.berry.schema

import dev.lyranie.berry.Constants
import dev.lyranie.berry.internal.Page
import dev.lyranie.berry.parser.schema.Field
import dev.lyranie.berry.parser.schema.Table
import dev.lyranie.commons.hash
import dev.lyranie.commons.reader
import dev.lyranie.commons.writer
import java.io.RandomAccessFile

fun Table.writeData() {
    writer {
        writeUTF(name)
        writeInt(fields.size)
        fields.forEach { it.writeData() }
    }
}

fun Table.writePage(writer: RandomAccessFile, pages: HashMap<Int, ArrayList<Page>>) {
    val page = Page(name, Constants.PAGE_SIZE, writer.channel.size())
    page.writeData()

    pages[name.hash()] = arrayListOf(page)
}

fun Table.Companion.readData(): Table = reader {
    val name = readUTF()
    val fieldsSize = readInt()
    val fields = mutableListOf<Field>()

    for (i in 0 until fieldsSize) {
        fields.add(Field.readData())
    }

    Table(name, fields)
}
