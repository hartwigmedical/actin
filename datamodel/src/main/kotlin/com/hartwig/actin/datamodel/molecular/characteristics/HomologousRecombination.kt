package com.hartwig.actin.datamodel.molecular.characteristics

import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence

data class HomologousRecombination(
    val isDeficient: Boolean,
    val score: Double?,
    val type: HomologousRecombinationType?,
    val brca1Value: Double?,
    val brca2Value: Double?,
    override val evidence: ClinicalEvidence
) : Actionable {
    override fun eventName(): String? {
        return if (isDeficient) MolecularCharacteristicEvents.HOMOLOGOUS_RECOMBINATION_DEFICIENT else null
    }
}
