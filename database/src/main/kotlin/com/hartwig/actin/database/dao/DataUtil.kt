package com.hartwig.actin.database.dao

import java.util.Objects

internal object DataUtil {

    private const val SEPARATOR = ";"

    fun toByte(bool: Boolean?): Byte? {
        return if (bool != null) (if (bool) 1 else 0).toByte() else null
    }

    fun concat(strings: Collection<String>?): String? {
        return strings?.sorted()?.joinToString(SEPARATOR)
    }

    fun concatObjects(objects: Collection<Any>?): String? {
        return concat(objects?.map(Objects::toString))
    }

    fun nullableToString(`object`: Any?): String? {
        return `object`?.toString()
    }
}