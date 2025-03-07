package com.hartwig.actin.datamodel.clinical

import java.time.LocalDate

data class Toxicity(
    override val name: String?,
    override val icdCodes: Set<IcdCode>,
    val evaluatedDate: LocalDate,
    val source: ToxicitySource,
    val grade: Int?,
    val endDate: LocalDate? = null
): Comorbidity {
    override val comorbidityClass = ComorbidityClass.TOXICITY
    override val year: Int?
        get() = evaluatedDate.year
    override val month: Int?
        get() = evaluatedDate.monthValue

    override fun withDefaultDate(date: LocalDate): Comorbidity = this
}