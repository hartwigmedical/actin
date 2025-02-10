package com.hartwig.actin.datamodel.clinical

import java.time.LocalDate

data class Complication(
    override val name: String?,
    override val icdCodes: Set<IcdCode>,
    override val year: Int? = null,
    override val month: Int? = null,
): Comorbidity {
    override val comorbidityClass = ComorbidityClass.COMPLICATION

    override fun withDefaultDate(date: LocalDate): Comorbidity = if (year != null) this else {
        copy(year = date.year, month = date.monthValue)
    }
}