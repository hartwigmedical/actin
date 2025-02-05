package com.hartwig.actin.datamodel.clinical

import java.time.LocalDate

data class Ecg(
    override val name: String?,
    val qtcfMeasure: EcgMeasure?,
    val jtcMeasure: EcgMeasure?,
    override val icdCodes: Set<IcdCode> = emptySet(),
    override val year: Int? = null,
    override val month: Int? = null
) : Comorbidity {
    override val comorbidityClass = ComorbidityClass.ECG

    override fun withDefaultDate(date: LocalDate): Comorbidity = if (year != null) this else {
        copy(year = date.year, month = date.monthValue)
    }
}
