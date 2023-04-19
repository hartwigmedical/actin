package com.hartwig.actin.soc.calendar

import java.time.LocalDate

object ReferenceDateProviderTestFactory {
    fun createCurrentDateProvider(): ReferenceDateProvider {
        val date = LocalDate.now()
        return object : ReferenceDateProvider {
            override fun date(): LocalDate {
                return date
            }

            override val isLive: Boolean
                get() = true
        }
    }
}