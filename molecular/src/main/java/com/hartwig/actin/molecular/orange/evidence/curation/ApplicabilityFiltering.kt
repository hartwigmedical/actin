package com.hartwig.actin.molecular.orange.evidence.curation

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Sets
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent
import com.hartwig.serve.datamodel.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.range.ActionableRange
import org.apache.logging.log4j.LogManager

object ApplicabilityFiltering {
    private val LOGGER = LogManager.getLogger(ApplicabilityFiltering::class.java)
    val NON_APPLICABLE_GENES: MutableSet<String?>? = Sets.newHashSet()
    val NON_APPLICABLE_AMPLIFICATIONS: MutableSet<String?>? = Sets.newHashSet()

    init {
        NON_APPLICABLE_GENES.add("CDKN2A")
        NON_APPLICABLE_GENES.add("TP53")
        NON_APPLICABLE_AMPLIFICATIONS.add("VEGFA")
    }

    fun isApplicable(actionableHotspot: ActionableHotspot): Boolean {
        return eventIsApplicable<ActionableHotspot?>(actionableHotspot.gene(), actionableHotspot)
    }

    fun isApplicable(actionableRange: ActionableRange): Boolean {
        return eventIsApplicable<ActionableRange?>(actionableRange.gene(), actionableRange)
    }

    fun isApplicable(actionableGene: ActionableGene): Boolean {
        if (actionableGene.event() == GeneEvent.AMPLIFICATION) {
            for (nonApplicableGene in NON_APPLICABLE_AMPLIFICATIONS) {
                if (actionableGene.gene() == nonApplicableGene) {
                    LOGGER.debug("Evidence for '{}' on gene {} is considered non-applicable",
                        actionableGene.sourceEvent(),
                        actionableGene.gene())
                    return false
                }
            }
        }
        return eventIsApplicable<ActionableGene?>(actionableGene.gene(), actionableGene)
    }

    @VisibleForTesting
    fun <T : ActionableEvent?> eventIsApplicable(gene: String, event: T): Boolean {
        if (NON_APPLICABLE_GENES.contains(gene)) {
            LOGGER.debug("Evidence for '{}' on gene {} is considered non-applicable", event.sourceEvent(), gene)
            return false
        }
        return true
    }
}
