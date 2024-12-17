package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable
import java.time.LocalDate

data class Toxicity(
    val name: String,
    override val icdCode: IcdCode,
    val evaluatedDate: LocalDate,
    val source: ToxicitySource,
    val grade: Int?,
    val endDate: LocalDate? = null
): IcdCodeHolder, Displayable {

    override fun display(): String {
        return name
    }
}