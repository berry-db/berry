package dev.lyranie.berry

import dev.lyranie.berry.internal.Page.Entry
import dev.lyranie.berry.internal.PageHandler
import dev.lyranie.berry.parser.exception.UnsupportedTypeException
import dev.lyranie.berry.parser.schema.Type
import dev.lyranie.commons.hash
import dev.lyranie.commons.reader
import dev.lyranie.commons.writer
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

@Suppress("DUPLICATES", "TooGenericExceptionThrown")
@PublishedApi
internal object BerryInternal {
    fun <T : Any> insert(klass: KClass<T>, value: T) {
        val name = klass.simpleName ?: throw RuntimeException("Unable to read table name from class: $klass")
        val table = BerryContext.schema.tables.find { it.name == name }!!
        val fields = table.fields.map { field ->
            val property = klass.memberProperties.find { it.name == field.name } ?: return@map null
            property.isAccessible = true
            val raw = property.getter.call(value)

            field.name to raw
        }

        val size = fields.sumOf {
            when (val v = it?.second) {
                is Int -> 4
                is String -> 2 + v.length
                is Boolean -> 1
                else -> throw UnsupportedTypeException(v!!::class)
            }
        }

        val (page, entryIndex) = PageHandler.getNextPage(name.hash(), size)
        val entry = if (entryIndex == -1) {
            null
        } else {
            page.entries[entryIndex]
        }

        writer {
            val dataOffset = entry?.dataOffset?.toLong() ?: filePointer
            seek(dataOffset)

            fields.forEach {
                when (it?.second) {
                    is String -> writeUTF(it.second as String)
                    is Int -> writeInt(it.second as Int)
                    is Boolean -> writeBoolean(it.second as Boolean)
                    else -> TODO("Unsupported")
                }
            }

            val length = filePointer - dataOffset
            page.add(dataOffset, length, entry)
        }
    }

    fun <T : Any> get(klass: KClass<T>, condition: (T) -> Boolean): List<T> {
        val name = klass.simpleName ?: throw RuntimeException("Unable to read table name from class: $klass")
        val table = BerryContext.schema.tables.find { it.name == name }!!
        val constructor = klass.primaryConstructor
            ?: throw RuntimeException("No primary constructor found for class: $klass")
        val fields = table.fields.mapNotNull { field ->
            val property = klass.memberProperties.find { it.name == field.name }
                ?: return@mapNotNull null
            property.isAccessible = true
            field
        }

        val list = arrayListOf<T>()

        PageHandler.pages[name.hash()]!!.forEach { page ->
            page.entries.forEach { entry ->
                if (entry.free) return@forEach

                reader {
                    seek(entry.dataOffset.toLong())

                    val values = mutableMapOf<KParameter, Any?>()
                    fields.forEach { field ->
                        val value = when (field.type) {
                            Type.STRING -> readUTF()
                            Type.NUMBER -> readInt()
                            Type.BOOLEAN -> readBoolean()
                            else -> TODO("Unsupported")
                        }
                        val param = constructor.parameters.find { it.name == field.name }
                        if (param != null) values[param] = value
                    }

                    val instance = constructor.callBy(values)
                    if (condition(instance)) list.add(instance)
                }
            }
        }

        return list
    }

    fun <T : Any> delete(klass: KClass<T>, condition: (T) -> Boolean) {
        val name = klass.simpleName ?: throw RuntimeException("Unable to read table name from class: $klass")
        val table = BerryContext.schema.tables.find { it.name == name }!!
        val constructor = klass.primaryConstructor
            ?: throw RuntimeException("No primary constructor found for class: $klass")
        val fields = table.fields.mapNotNull { field ->
            val property = klass.memberProperties.find { it.name == field.name }
                ?: return@mapNotNull null
            property.isAccessible = true
            field
        }

        PageHandler.pages[name.hash()]!!.forEach { page ->
            val toRemove = mutableListOf<Entry>()

            page.entries.forEach { entry ->
                if (entry.free) return@forEach

                reader {
                    seek(entry.dataOffset.toLong())

                    val values = mutableMapOf<KParameter, Any?>()
                    fields.forEach { field ->
                        val value = when (field.type) {
                            Type.STRING -> readUTF()
                            Type.NUMBER -> readInt()
                            Type.BOOLEAN -> readBoolean()
                            else -> TODO("Unsupported")
                        }
                        val param = constructor.parameters.find { it.name == field.name }
                        if (param != null) values[param] = value
                    }

                    val instance = constructor.callBy(values)
                    if (condition(instance)) {
                        toRemove.add(entry)
                    }
                }
            }

            toRemove.forEach { entry ->
                entry.free(page)
            }
        }
    }
}
