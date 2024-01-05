package com.hartwig.actin.molecular.datamodel.evidence

data class ActionableEvidence(
    val approvedTreatments: Set<String> = emptySet(),
    val externalEligibleTrials: Set<String> = emptySet(),
    val onLabelExperimentalTreatments: Set<String> = emptySet(),
    val offLabelExperimentalTreatments: Set<String> = emptySet(),
    val preClinicalTreatments: Set<String> = emptySet(),
    val knownResistantTreatments: Set<String> = emptySet(),
    val suspectResistantTreatments: Set<String> = emptySet()
) {

    operator fun plus(other: ActionableEvidence): ActionableEvidence {
        return ActionableEvidence(
            approvedTreatments + other.approvedTreatments,
            externalEligibleTrials + other.externalEligibleTrials,
            onLabelExperimentalTreatments + other.onLabelExperimentalTreatments,
            offLabelExperimentalTreatments + other.offLabelExperimentalTreatments,
            preClinicalTreatments + other.preClinicalTreatments,
            knownResistantTreatments + other.knownResistantTreatments,
            suspectResistantTreatments + other.suspectResistantTreatments
        )
    }
}
