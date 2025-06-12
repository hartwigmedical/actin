package com.hartwig.actin.molecular.evidence.curation

import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.molecular.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.molecular.range.ActionableRange

object ApplicabilityFiltering {

    val NON_APPLICABLE_GENES = setOf("CDKN2A", "TP53")
    val NON_APPLICABLE_AMPLIFICATIONS = setOf("VEGFA")

    fun isApplicable(actionableHotspot: ActionableHotspot): Boolean {
        return eventIsApplicable(actionableHotspot.variants().first().gene())
    }

    fun isApplicable(actionableRange: ActionableRange): Boolean {
        return eventIsApplicable(actionableRange.gene())
    }

    fun isApplicable(actionableGene: ActionableGene): Boolean {
        if (actionableGene.event() == GeneEvent.AMPLIFICATION) {
            for (nonApplicableGene in NON_APPLICABLE_AMPLIFICATIONS) {
                if (actionableGene.gene() == nonApplicableGene) {
                    return false
                }
            }
        }
        return eventIsApplicable(actionableGene.gene())
    }

    private fun eventIsApplicable(gene: String): Boolean {
        return !NON_APPLICABLE_GENES.contains(gene)
    }
}
