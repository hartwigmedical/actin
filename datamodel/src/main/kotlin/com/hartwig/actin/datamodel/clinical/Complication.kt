package com.hartwig.actin.datamodel.clinical

data class Complication(
    override val name: String,
    override val year: Int? = null,
    override val month: Int? = null,
    override val icdCodes: Set<IcdCode>,
): Comorbidity {
    override val comorbidityClass = ComorbidityClass.COMPLICATION

    override fun withDefaultYearAndMonth(defaultYear: Int, defaultMonth: Int): Comorbidity = if (year != null) this else {
        copy(year = defaultYear, month = defaultMonth)
    }
}