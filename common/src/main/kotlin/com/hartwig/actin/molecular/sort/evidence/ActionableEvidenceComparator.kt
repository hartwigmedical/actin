package com.hartwig.actin.molecular.sort.evidence

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence

class ActionableEvidenceComparator : Comparator<ActionableEvidence> {

    override fun compare(evidence1: ActionableEvidence, evidence2: ActionableEvidence): Int {
        listOf(
            ActionableEvidence::approvedTreatments,
            ActionableEvidence::externalEligibleTrials,
            ActionableEvidence::onLabelExperimentalTreatments,
            ActionableEvidence::offLabelExperimentalTreatments,
            ActionableEvidence::preClinicalTreatments,
            ActionableEvidence::knownResistantTreatments,
            ActionableEvidence::suspectResistantTreatments
        ).forEach { retrieveCollection ->
            val comparison = retrieveCollection.invoke(evidence2).size.compareTo(retrieveCollection.invoke(evidence1).size)
            if (comparison != 0) {
                return comparison
            }
        }
        return 0
    }
}
