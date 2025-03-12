package com.hartwig.actin.molecular.evidence.curation

import com.hartwig.serve.datamodel.molecular.ActionableEvent
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.molecular.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.molecular.range.ActionableRange
import org.apache.logging.log4j.LogManager

object ApplicabilityFiltering {

    private val LOGGER = LogManager.getLogger(ApplicabilityFiltering::class.java)

    val NON_APPLICABLE_GENES = setOf("CDKN2A", "TP53")
    val NON_APPLICABLE_AMPLIFICATIONS = setOf("VEGFA")

    fun isApplicable(actionableHotspot: ActionableHotspot): Boolean {
        return eventIsApplicable(actionableHotspot.variants().first().gene(), actionableHotspot)
    }

    fun isApplicable(actionableRange: ActionableRange): Boolean {
        return eventIsApplicable(actionableRange.gene(), actionableRange)
    }

    fun isApplicable(actionableGene: ActionableGene): Boolean {
        if (actionableGene.event() == GeneEvent.AMPLIFICATION) {
            for (nonApplicableGene in NON_APPLICABLE_AMPLIFICATIONS) {
                if (actionableGene.gene() == nonApplicableGene) {
                    LOGGER.debug(
                        "Evidence for '{}' on gene {} is considered non-applicable",
                        actionableGene.sourceEvent(),
                        actionableGene.gene()
                    )
                    return false
                }
            }
        }
        return eventIsApplicable(actionableGene.gene(), actionableGene)
    }

    private fun <T : ActionableEvent> eventIsApplicable(gene: String, event: T): Boolean {
        if (NON_APPLICABLE_GENES.contains(gene)) {
            LOGGER.debug("Evidence for '{}' on gene {} is considered non-applicable", event.sourceEvent(), gene)
            return false
        }
        return true
    }
}
