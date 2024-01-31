package com.hartwig.actin.molecular.orange.evidence.actionability

import com.hartwig.actin.molecular.orange.evidence.matching.GeneMatching
import com.hartwig.actin.molecular.orange.evidence.matching.HotspotMatching
import com.hartwig.actin.molecular.orange.evidence.matching.RangeMatching
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent
import com.hartwig.serve.datamodel.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.range.ActionableRange

internal class VariantEvidence private constructor(
    private val actionableHotspots: List<ActionableHotspot>,
    private val actionableRanges: List<ActionableRange>, private val applicableActionableGenes: List<ActionableGene>
) : EvidenceMatcher<PurpleVariant> {

    override fun findMatches(event: PurpleVariant): List<ActionableEvent> {
        return listOf(hotspotMatches(event), rangeMatches(event), geneMatches(event)).flatten()
    }

    private fun hotspotMatches(variant: PurpleVariant): List<ActionableEvent> {
        return filterMatchingEvents(actionableHotspots, variant, HotspotMatching::isMatch)
    }

    private fun rangeMatches(variant: PurpleVariant): List<ActionableEvent> {
        return filterMatchingEvents(actionableRanges, variant, RangeMatching::isMatch)
    }

    private fun geneMatches(variant: PurpleVariant): List<ActionableEvent> {
        return filterMatchingEvents(applicableActionableGenes, variant, GeneMatching::isMatch)
    }

    private fun <T : ActionableEvent> filterMatchingEvents(
        events: List<T>, variant: PurpleVariant, predicate: (T, PurpleVariant) -> Boolean
    ): List<ActionableEvent> {
        return if (!variant.reported()) emptyList() else events.filter { predicate.invoke(it, variant) }
    }

    companion object {
        private val APPLICABLE_GENE_EVENTS = setOf(GeneEvent.ACTIVATION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(actionableEvents: ActionableEvents): VariantEvidence {
            val applicableActionableGenes = actionableEvents.genes()
                .filter { actionableGene: ActionableGene -> APPLICABLE_GENE_EVENTS.contains(actionableGene.event()) }
            val ranges = listOf(actionableEvents.codons(), actionableEvents.exons()).flatten()
            return VariantEvidence(actionableEvents.hotspots(), ranges, applicableActionableGenes)
        }
    }
}