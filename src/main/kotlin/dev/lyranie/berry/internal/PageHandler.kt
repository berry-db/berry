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
import dev.lyranie.berry.internal.Page.Entry
import dev.lyranie.commons.FileUtils
import dev.lyranie.commons.debug
import dev.lyranie.commons.hash
import dev.lyranie.commons.reader
import dev.lyranie.commons.writer

internal object PageHandler {
    val pages: HashMap<Int, ArrayList<Page>> = HashMap()

    fun init() {
        writer {
            val offset = channel.size()

            seek(Constants.PAGE_SECTION_OFFSET_POS.toLong())
            writeInt(offset.toInt())
            seek(offset)

            writeInt(BerryContext.schema.tables.size)
            BerryContext.schema.tables.forEach {
                val page = Page(it.name.hash(), filePointer)
                page.writeData()

                pages[it.name.hash()] = arrayListOf(page)
            }
        }
    }

    fun read() {
        debug<Berry>("Reading pages")
        reader {
            seek(Constants.PAGE_SECTION_OFFSET_POS.toLong())
            val offset = readInt()
            seek(offset.toLong())

            debug<Berry>("Page offset at 0x{}", offset.toHexString())

            val size = readInt()

            for (i in 0 until size) {
                val page = Page.readData()
                pages[page.hash] = arrayListOf(page)
            }

            pages.forEach { (_, pages) ->
                var lastPage = pages.last()

                while (lastPage.nextPage != -1) {
                    FileUtils.readPointer = lastPage.nextPage.toLong()

                    val nextPage = Page.readData(lastPage.nextPage)
                    pages.add(nextPage)
                    lastPage = nextPage
                }
            }

            seek(channel.size())
        }
    }

    fun getNextPage(hash: Int, entrySize: Int): Pair<Page, Int> {
        val page = pages[hash]!!.firstOrNull { page ->
            page.entries.any { entry -> entry.free && entry.dataLength == entrySize }
        }

        if (page != null) {
            val entryIndex = page.entries.indexOfFirst { entry -> entry.free && entry.dataLength == entrySize }
            return page to entryIndex
        }

        val lastPage = pages[hash]?.last() ?: throw IllegalStateException("No pages for hash $hash")
        if (lastPage.entries.size != (Constants.PAGE_SIZE - 20) / Entry.SIZE) return lastPage to -1

        return createPage(hash, lastPage) to -1
    }

    fun createPage(hash: Int, lastPage: Page): Page = writer {
        val page = Page.create(hash)

        pages[hash]!!.add(page)

        lastPage.nextPage = page.offset.toInt()
        seek(lastPage.offset + 4)
        writeInt(page.offset.toInt())

        seek(channel.size())

        page
    }

    fun getPage(hash: Int, index: Int) = pages[hash]!![index]
}
