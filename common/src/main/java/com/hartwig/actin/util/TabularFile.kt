package com.hartwig.actin.util

import com.google.common.collect.Maps

object TabularFile {
    val DELIMITER: String = "\t"

    @JvmStatic
    fun createFields(header: Array<String>): Map<String, Int> {
        val fields: MutableMap<String, Int> = Maps.newHashMap()
        for (i in header.indices) {
            fields.put(header.get(i), i)
        }
        return fields
    }
}
