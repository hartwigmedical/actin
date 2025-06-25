package com.hartwig.actin.datamodel.clinical

import java.time.LocalDate

data class OtherCondition(
    override val name: String?,
    override val icdCodes: Set<IcdCode>,
    override val year: Int? = null,
    override val month: Int? = null
) : Comorbidity {
    constructor(
        baseComorbidity: BaseComorbidity
    ) : this(
        baseComorbidity.name,
        baseComorbidity.icdCodes,
        baseComorbidity.year,
        baseComorbidity.month
    )

    override val comorbidityClass = ComorbidityClass.OTHER_CONDITION

    override fun withDefaultDate(date: LocalDate): Comorbidity = if (year != null) this else {
        copy(year = date.year, month = date.monthValue)
    }
}