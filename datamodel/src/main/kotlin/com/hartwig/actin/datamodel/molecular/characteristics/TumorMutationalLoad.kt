package com.hartwig.actin.datamodel.molecular.characteristics

import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence

data class TumorMutationalLoad(
    val score: Int,
    val isHigh: Boolean,
    override val evidence: ClinicalEvidence
) : Actionable
