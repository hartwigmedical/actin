package com.hartwig.actin.datamodel.clinical

import java.time.LocalDate

data class OtherCondition(
    override val name: String?,
    override val year: Int? = null,
    override val month: Int? = null,
    override val icdCodes: Set<IcdCode>,
): Comorbidity {
    override val comorbidityClass = ComorbidityClass.OTHER_CONDITION

    override fun withDefaultDate(date: LocalDate): Comorbidity = if (year != null) this else {
        copy(year = date.year, month = date.monthValue)
    }
}