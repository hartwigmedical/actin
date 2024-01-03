package com.hartwig.actin.molecular.interpretation

data class AggregatedEvidence(
    val approvedTreatmentsPerEvent: Map<String, List<String>> = emptyMap(),
    val externalEligibleTrialsPerEvent: Map<String, List<String>> = emptyMap(),
    val onLabelExperimentalTreatmentsPerEvent: Map<String, List<String>> = emptyMap(),
    val offLabelExperimentalTreatmentsPerEvent: Map<String, List<String>> = emptyMap(),
    val preClinicalTreatmentsPerEvent: Map<String, List<String>> = emptyMap(),
    val knownResistantTreatmentsPerEvent: Map<String, List<String>> = emptyMap(),
    val suspectResistantTreatmentsPerEvent: Map<String, List<String>> = emptyMap()
)
