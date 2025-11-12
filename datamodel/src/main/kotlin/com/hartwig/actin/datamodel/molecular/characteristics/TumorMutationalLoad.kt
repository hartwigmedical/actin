package com.hartwig.actin.datamodel.molecular.characteristics

import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_LOAD
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristicEvents.LOW_TUMOR_MUTATIONAL_LOAD
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence

data class TumorMutationalLoad(
    val score: Int,
    val isHigh: Boolean,
    override val evidence: ClinicalEvidence
) : Actionable {
    override fun eventName(): String {
        return if (isHigh) HIGH_TUMOR_MUTATIONAL_LOAD else LOW_TUMOR_MUTATIONAL_LOAD
    }
}
