package com.hartwig.actin.datamodel.molecular.characteristics

import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_BURDEN
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristicEvents.LOW_TUMOR_MUTATIONAL_BURDEN
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence

data class TumorMutationalBurden(
    val score: Double,
    val isHigh: Boolean,
    override val evidence: ClinicalEvidence
) : Actionable {
    override fun eventName(): String {
        return if (isHigh) HIGH_TUMOR_MUTATIONAL_BURDEN else LOW_TUMOR_MUTATIONAL_BURDEN
    }
}
