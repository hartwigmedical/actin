package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable
import java.time.LocalDate

data class Toxicity(
    val name: String,
    override val icdCodes: Set<IcdCode>,
    val evaluatedDate: LocalDate,
    val source: ToxicitySource,
    val grade: Int?,
    val endDate: LocalDate? = null
): IcdCodeEntity, Displayable {

    override fun display(): String {
        return name
    }
}