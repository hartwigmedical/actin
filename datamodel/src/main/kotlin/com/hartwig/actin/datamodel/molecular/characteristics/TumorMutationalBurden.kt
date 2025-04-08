package com.hartwig.actin.datamodel.molecular.characteristics

import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence

data class TumorMutationalBurden(
    val score: Double,
    val isHigh: Boolean,
    override val evidence: ClinicalEvidence
) : Actionable
