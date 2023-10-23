package com.hartwig.actin.database.dao

internal object DataUtil {
    private const val SEPARATOR = ";"

    fun toByte(bool: Boolean?): Byte? {
        return if (bool != null) (if (bool) 1 else 0).toByte() else null
    }

    fun concat(strings: Collection<String>?): String? {
        return strings?.joinToString(SEPARATOR)
    }

    fun <T> concatObjects(objects: Collection<T>?): String? {
        return concat(objects?.map { obj: T -> obj.toString() })
    }

    fun nullableToString(`object`: Any?): String? {
        return `object`?.toString()
    }
}