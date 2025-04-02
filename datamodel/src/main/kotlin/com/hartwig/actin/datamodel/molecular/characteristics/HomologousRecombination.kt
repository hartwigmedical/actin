package com.hartwig.actin.datamodel.molecular.characteristics

import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence

data class HomologousRecombination(
    val score: Double,
    val isDeficient: Boolean,
    val type: HomologousRecombinationType,
    val brca1Value: Double,
    val brca2Value: Double,
    override val evidence: ClinicalEvidence
) : Actionable
