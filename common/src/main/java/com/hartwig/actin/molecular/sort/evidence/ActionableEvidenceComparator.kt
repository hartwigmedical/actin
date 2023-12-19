package com.hartwig.actin.molecular.sort.evidence

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence

class ActionableEvidenceComparator : Comparator<ActionableEvidence> {
    override fun compare(evidence1: ActionableEvidence, evidence2: ActionableEvidence): Int {
        val approvedCompare = Integer.compare(evidence2.approvedTreatments().size, evidence1.approvedTreatments().size)
        if (approvedCompare != 0) {
            return approvedCompare
        }
        val externalTrialCompare = Integer.compare(evidence2.externalEligibleTrials().size, evidence1.externalEligibleTrials().size)
        if (externalTrialCompare != 0) {
            return externalTrialCompare
        }
        val onLabelCompare = Integer.compare(evidence2.onLabelExperimentalTreatments().size, evidence1.onLabelExperimentalTreatments().size)
        if (onLabelCompare != 0) {
            return onLabelCompare
        }
        val offLabelCompare =
            Integer.compare(evidence2.offLabelExperimentalTreatments().size, evidence1.offLabelExperimentalTreatments().size)
        if (offLabelCompare != 0) {
            return offLabelCompare
        }
        val preClinicalCompare = Integer.compare(evidence2.preClinicalTreatments().size, evidence1.preClinicalTreatments().size)
        if (preClinicalCompare != 0) {
            return preClinicalCompare
        }
        val knownResistantCompare = Integer.compare(evidence2.knownResistantTreatments().size, evidence1.knownResistantTreatments().size)
        return if (knownResistantCompare != 0) {
            knownResistantCompare
        } else Integer.compare(
            evidence2.suspectResistantTreatments().size,
            evidence1.suspectResistantTreatments().size
        )
    }
}
