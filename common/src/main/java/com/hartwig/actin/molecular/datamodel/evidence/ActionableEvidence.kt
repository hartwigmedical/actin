package com.hartwig.actin.molecular.datamodel.evidence

data class ActionableEvidence(
    val approvedTreatments: Set<String> = emptySet(),
    val externalEligibleTrials: Set<String> = emptySet(),
    val onLabelExperimentalTreatments: Set<String> = emptySet(),
    val offLabelExperimentalTreatments: Set<String> = emptySet(),
    val preClinicalTreatments: Set<String> = emptySet(),
    val knownResistantTreatments: Set<String> = emptySet(),
    val suspectResistantTreatments: Set<String> = emptySet()
)
