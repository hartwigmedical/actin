package com.hartwig.actin.molecular.sort.evidence

import com.hartwig.actin.molecular.datamodel.evidence.ClinicalEvidence

class ActionableEvidenceComparator : Comparator<ClinicalEvidence> {

    override fun compare(evidence1: ClinicalEvidence, evidence2: ClinicalEvidence): Int {
        listOf(
            ClinicalEvidence::approvedTreatments,
            ClinicalEvidence::externalEligibleTrials,
            ClinicalEvidence::onLabelExperimentalTreatments,
            ClinicalEvidence::offLabelExperimentalTreatments,
            ClinicalEvidence::preClinicalTreatments,
            ClinicalEvidence::knownResistantTreatments,
            ClinicalEvidence::suspectResistantTreatments
        ).forEach { retrieveCollection ->
            val comparison = retrieveCollection.invoke(evidence2).size.compareTo(retrieveCollection.invoke(evidence1).size)
            if (comparison != 0) {
                return comparison
            }
        }
        return 0
    }
}
