package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.molecular.interpretation.ActionableAndEvidenceFactory.createTreatmentEvidences
import com.hartwig.actin.molecular.interpretation.ActionableAndEvidenceFactory.createTrialEvidences

object AggregatedEvidenceFactory {

    fun create(molecular: MolecularTest): AggregatedEvidence {
        return AggregatedEvidence(
            mapByEvent(createTreatmentEvidences(molecular)), mapByEvent(createTrialEvidences(molecular))
        )
    }

    private fun <E> mapByEvent(actionableAndEvidences: List<Pair<Actionable, Set<E>>>): Map<String, Set<E>> {
        return actionableAndEvidences.groupBy({ it.first.eventName()!! }, { it.second })
            .mapValues { (_, values) -> values.flatten().toSet() }
    }
}
