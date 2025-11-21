package com.hartwig.actin.datamodel.molecular.characteristics

import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence

data class TumorMutationalLoad(
    val score: Int,
    val isHigh: Boolean,
    override val evidence: ClinicalEvidence,
    override val event: String = if (isHigh) MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_LOAD else MolecularCharacteristicEvents.LOW_TUMOR_MUTATIONAL_LOAD
) : Actionable