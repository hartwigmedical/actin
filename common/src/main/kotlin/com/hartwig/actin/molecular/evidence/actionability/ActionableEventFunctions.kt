package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.molecular.ActionableEvent
import com.hartwig.serve.datamodel.molecular.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.molecular.fusion.ActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.molecular.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.molecular.immuno.ActionableHLA
import com.hartwig.serve.datamodel.molecular.range.ActionableRange

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
