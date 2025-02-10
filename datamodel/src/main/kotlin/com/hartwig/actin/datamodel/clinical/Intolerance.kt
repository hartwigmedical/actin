package com.hartwig.actin.datamodel.clinical

import java.time.LocalDate

data class Intolerance(
    override val name: String?,
    override val icdCodes: Set<IcdCode>,
    val type: String? = null,
    val clinicalStatus: String? = null,
    val verificationStatus: String? = null,
    val criticality: String? = null,
    override val year: Int? = null,
    override val month: Int? = null
): Comorbidity {
    override val comorbidityClass = ComorbidityClass.INTOLERANCE

    override fun withDefaultDate(date: LocalDate): Comorbidity = if (year != null) this else {
        copy(year = date.year, month = date.monthValue)
    }
}