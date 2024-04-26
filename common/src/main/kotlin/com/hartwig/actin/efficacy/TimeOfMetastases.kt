package com.hartwig.actin.efficacy

import com.hartwig.actin.Displayable

enum class TimeOfMetastases(private val display: String) : Displayable {
    SYNCHRONOUS("Synchronous"),
    METACHRONOUS("Metachronous"),
    BOTH("Synchronous and metachronous"),
    UNKNOWN("Unknown"),
    NONE("None");

    override fun display(): String {
        return display
    }
}