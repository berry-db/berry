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

package dev.lyranie.berry.internal

import dev.lyranie.berry.Berry
import dev.lyranie.berry.BerryContext
import dev.lyranie.berry.Constants
import dev.lyranie.berry.internal.exception.MalformedDatabaseException
import dev.lyranie.commons.debug
import dev.lyranie.commons.hash
import dev.lyranie.commons.reader
import dev.lyranie.commons.writer

data class Page(
    val hash: Int,
    val offset: Long,
    val entries: ArrayList<Entry> = ArrayList(),
    var nextPage: Int = -1,
) {
    constructor(
        name: String,
        size: Int,
        offset: Long,
        entries: ArrayList<Entry> = ArrayList(),
        nextPage: Int = -1,
    ) : this(name.hash(), offset, entries, nextPage)

    data class Entry(
        val entryOffset: Int,
        val dataOffset: Int,
        val dataLength: Int,
        val free: Boolean = false,
    ) {
        fun writeData() {
            writer {
                writeInt(entryOffset)
                writeInt(dataOffset)
                writeInt(dataLength)
                writeBoolean(free)
            }
        }

        fun free(parent: Page) {
            val self = this

            writer {
                seek(entryOffset.toLong() + FREE_OFFSET)
                writeBoolean(true)

                val index = parent.entries.indexOf(self)
                if (index != -1) {
                    parent.entries[index] = self.copy(free = true)
                }
            }
        }

        companion object {
            const val SIZE = 13
            const val ENTRY_OFFSET = 0
            const val DATA_OFFSET = 4
            const val LENGTH = 8
            const val FREE_OFFSET = 12

            fun readData(): Entry = reader {
                val entryOffset = readInt()
                val dataOffset = readInt()
                val dataLength = readInt()
                val free = readBoolean()

                Entry(entryOffset, dataOffset, dataLength, free)
            }
        }
    }

    fun writeData() {
        writer {
            val offset = filePointer

            writeInt(hash)
            writeInt(nextPage)
            writeInt(offset.toInt())
            writeInt(entries.size)

            entries.forEach { it.writeData() }

            val bytesWritten = 16 + (entries.size * Entry.SIZE)
            val padding = Constants.PAGE_SIZE - bytesWritten
            repeat(padding) { writeByte(0) }
        }
    }

    fun add(dataOffset: Long, dataLength: Long, existingEntry: Entry?) {
        if (existingEntry != null) {
            return
        }

        val thisOffset = this.offset

        val pageOffset = 16 + (entries.size * Entry.SIZE)
        val entryOffset = thisOffset + pageOffset
        val entry = Entry(entryOffset.toInt(), dataOffset.toInt(), dataLength.toInt())

        writer {
            seek(thisOffset + pageOffset)

            entry.writeData()
            entries.add(entry)

            seek(thisOffset + 12)
            writeInt(entries.size)

            seek(dataOffset + dataLength)
        }
    }

    companion object {
        fun readData(offset: Int? = null): Page {
            debug<Berry>("------------------------------------")
            debug<Berry>("Reading Page")

            return reader {
                offset?.let { seek(it.toLong()) }

                val hash = readInt()
                debug<Berry>("Page hash: {}", hash)

                val name = BerryContext.schema.tables.firstOrNull { it.name.hash() == hash }?.name
                    ?: throw MalformedDatabaseException()
                debug<Berry>("Table name: {}", name)

                val nextPage = readInt()
                debug<Berry>("Next page: {}", nextPage)

                val offset = readInt()
                debug<Berry>("Page offset: {}", offset)

                val entriesSize = readInt()
                debug<Berry>("Page entries: {}", entriesSize)

                val entries = ArrayList<Entry>()

                for (i in 0 until entriesSize) {
                    entries.add(Entry.readData())
                }

                val bytesRead = 16 + (entriesSize * Entry.SIZE)
                val bytesToSkip = Constants.PAGE_SIZE - bytesRead
                skipBytes(bytesToSkip)

                Page(name, Constants.PAGE_SIZE, offset.toLong(), entries, nextPage)
            }
        }

        fun create(hash: Int): Page = writer {
            val offset = filePointer
            val page = Page(hash, offset)

            page.writeData()
            page
        }
    }
}
