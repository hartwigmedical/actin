package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.ComorbidityClass
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.OtherCondition
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import java.time.LocalDate

data class ToxicityCuration(
    override val name: String?,
    override val icdCodes: Set<IcdCode>,
    val grade: Int?
) : Comorbidity {

    override val month = null
    override val year = null
    override val comorbidityClass = ComorbidityClass.TOXICITY
    override fun withDefaultDate(date: LocalDate): Comorbidity = Toxicity(name, icdCodes, date, ToxicitySource.EHR, grade)
}