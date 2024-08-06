package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.ActionableEvent

data class ActionabilityMatch(
    val onLabelEvents: List<ActionableEvent>,
    val offLabelEvents: List<ActionableEvent>
)
