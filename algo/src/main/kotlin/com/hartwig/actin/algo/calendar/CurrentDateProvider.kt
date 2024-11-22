package com.hartwig.actin.algo.calendar

import java.time.LocalDate

internal class CurrentDateProvider : ReferenceDateProvider {

    private val currentDate = LocalDate.now()

    override fun date(): LocalDate {
        return currentDate
    }

    override val isLive: Boolean
        get() = true
}