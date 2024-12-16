package com.hartwig.actin.datamodel.clinical

import java.time.LocalDate

data class Toxicity(
    val name: String,
    override val icdCode: IcdCode,
    val evaluatedDate: LocalDate,
    val source: ToxicitySource,
    val grade: Int?,
    val endDate: LocalDate? = null
): IcdCodeHolder