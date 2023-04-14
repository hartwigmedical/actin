package com.hartwig.actin.soc.calendar

import java.time.LocalDate

interface ReferenceDateProvider {
    fun date(): LocalDate
    val isLive: Boolean
    fun year(): Int {
        return date().year
    }

    fun month(): Int {
        return date().monthValue
    }
}