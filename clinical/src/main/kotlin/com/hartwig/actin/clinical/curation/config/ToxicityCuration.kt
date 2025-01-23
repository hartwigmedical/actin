package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.ComorbidityClass
import com.hartwig.actin.datamodel.clinical.IcdCode

data class ToxicityCuration(
    override val name: String,
    override val icdCodes: Set<IcdCode>,
    val grade: Int?
) : Comorbidity {

    override val month = null
    override val year = null
    override val comorbidityClass = ComorbidityClass.TOXICITY
    override fun withDefaultYearAndMonth(year: Int, month: Int): Comorbidity = withDefaultYearAndMonth(year = year, month = month)
}