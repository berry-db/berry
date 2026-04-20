package dev.lyranie.berry

import dev.lyranie.berry.parser.schema.Schema

internal object BerryContext {
    lateinit var schema: Schema

    var pagePointer = 0L
}
