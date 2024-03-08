package com.hartwig.actin.util

object TabularFile {

    const val DELIMITER: String = "\t"

    fun createFields(header: Array<String>): Map<String, Int> {
        return header.zip(header.indices).toMap()
    }
}
