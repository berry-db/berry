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

package dev.lyranie.berry

import com.google.gson.Gson
import dev.lyranie.berry.internal.PageHandler
import dev.lyranie.berry.parser.SchemaParser
import dev.lyranie.berry.parser.schema.Schema
import dev.lyranie.berry.schema.readData
import dev.lyranie.berry.schema.writeData
import dev.lyranie.commons.FileUtils
import dev.lyranie.commons.debug
import dev.lyranie.commons.info
import dev.lyranie.commons.reader
import dev.lyranie.commons.warn
import dev.lyranie.commons.writer
import java.io.File
import kotlin.system.exitProcess

class Berry private constructor() {
    companion object {
        fun open(path: String) = open(File(path))

        fun open(file: File): Berry {
            if (!file.exists()) {
                writeFile(file)
            } else {
                readFile(file)
            }

            if (file.extension != "bdb") {
                warn<Berry>("File does not match bdb file extension (${file.extension})")
            }

            return Berry()
        }

        fun writeFile(file: File) {
            warn<Berry>("File does not exist: $file, creating...")

            file.createNewFile()

            FileUtils.file = file

            writeMetadata()

            BerryContext.schema = SchemaParser.parse(File("schemas/example.bdbs")) // change
            BerryContext.schema.writeData()

            PageHandler.init()
        }

        fun readFile(file: File) {
            FileUtils.file = file

            readMetadata()

            val schema = Schema.readData()
            info<Berry>("Schema: {}", Gson().toJson(schema))
            BerryContext.schema = schema

            PageHandler.read()

            info<Berry>("Read {} pages", PageHandler.pages.entries.sumOf { it.value.size })
        }

        private fun writeMetadata() {
            writer {
                Constants.HEADER.map(Byte::toInt).forEach {
                    writeByte(it)
                }

                writeInt(-1)
            }
        }

        private fun readMetadata() {
            reader {
                val header = readNBytes(4)
                if (!header.contentEquals(Constants.HEADER)) {
                    info<Berry>("Invalid header > aborting")

                    close()
                    exitProcess(1)
                }

                info<Berry>("File Version: {}", header.last().toInt().toChar())

                val pagePointer = readInt()
                debug<Berry>("Page pointer: 0x{}", pagePointer.toHexString())
                BerryContext.pagePointer = pagePointer.toLong()
            }
        }
    }

    inline fun <reified T : Any> insert(value: T) = BerryInternal.insert(T::class, value)

    inline fun <reified T : Any> get(noinline condition: (T) -> Boolean = { true }): List<T> =
        BerryInternal.get(T::class, condition)

    inline fun <reified T : Any> delete(noinline condition: (T) -> Boolean = { true }) =
        BerryInternal.delete(T::class, condition)
}
