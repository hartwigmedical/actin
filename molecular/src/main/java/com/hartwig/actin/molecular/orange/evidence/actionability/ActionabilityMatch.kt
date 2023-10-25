package com.hartwig.actin.molecular.orange.evidence.actionability

import com.hartwig.serve.datamodel.ActionableEvent

data class ActionabilityMatch(
    val onLabelEvents: MutableList<ActionableEvent>,
    val offLabelEvents: MutableList<ActionableEvent>,
)
