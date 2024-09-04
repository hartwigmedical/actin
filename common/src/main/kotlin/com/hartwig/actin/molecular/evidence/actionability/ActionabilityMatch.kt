package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.fusion.ActionableFusion
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent
import com.hartwig.serve.datamodel.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.immuno.ActionableHLA
import com.hartwig.serve.datamodel.range.ActionableRange

fun ActionableEvent.isCategoryEvent(): Boolean {
    return when (this) {

        is ActionableHotspot,
        is ActionableHLA,
        is ActionableCharacteristic,
        is ActionableFusion -> false

        is ActionableRange -> true

        is ActionableGene -> this.event() !in setOf(
            GeneEvent.AMPLIFICATION,
            GeneEvent.DELETION,
            GeneEvent.OVEREXPRESSION,
            GeneEvent.UNDEREXPRESSION,
            GeneEvent.PRESENCE_OF_PROTEIN,
            GeneEvent.ABSENCE_OF_PROTEIN
        )

        else -> throw IllegalArgumentException()
    }
}

data class ActionabilityMatch(
    val onLabelEvents: List<ActionableEvent>,
    val offLabelEvents: List<ActionableEvent>
)
