package dev.lyranie.berry

internal object Constants {
    const val VERSION = '1'
    val HEADER = byteArrayOf('B', 'D', 'B', VERSION)

    val PAGE_SECTION_OFFSET_POS: Int = HEADER.size
    const val PAGE_SIZE = 4096
}
