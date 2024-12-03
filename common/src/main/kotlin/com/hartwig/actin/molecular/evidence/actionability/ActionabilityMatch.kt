package com.hartwig.actin.molecular.evidence.actionability

data class ActionabilityMatch(
    val onLabelEvidence: ActionableEvents,
    val offLabelEvidence: ActionableEvents
)
