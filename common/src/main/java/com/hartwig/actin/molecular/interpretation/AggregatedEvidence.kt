package com.hartwig.actin.molecular.interpretation

data class AggregatedEvidence(
    val approvedTreatmentsPerEvent: Map<String, Set<String>> = emptyMap(),
    val externalEligibleTrialsPerEvent: Map<String, Set<String>> = emptyMap(),
    val onLabelExperimentalTreatmentsPerEvent: Map<String, Set<String>> = emptyMap(),
    val offLabelExperimentalTreatmentsPerEvent: Map<String, Set<String>> = emptyMap(),
    val preClinicalTreatmentsPerEvent: Map<String, Set<String>> = emptyMap(),
    val knownResistantTreatmentsPerEvent: Map<String, Set<String>> = emptyMap(),
    val suspectResistantTreatmentsPerEvent: Map<String, Set<String>> = emptyMap()
)
