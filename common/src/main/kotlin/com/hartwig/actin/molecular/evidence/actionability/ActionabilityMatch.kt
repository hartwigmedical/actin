package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.fusion.ActionableFusion
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.range.ActionableRange

fun ActionableEvent.isCategoryEvent(): Boolean? {
    return when (this) {
        is ActionableHotspot -> false

        is ActionableRange,
        is ActionableGene,
        is ActionableFusion -> true

        else -> null
    }
}

data class ActionabilityMatch(
    val onLabelEvents: List<ActionableEvent>,
    val offLabelEvents: List<ActionableEvent>
)
