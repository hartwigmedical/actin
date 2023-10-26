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
import java.util.Set
import java.util.function.BiPredicate
import java.util.stream.Collectors
import java.util.stream.Stream

internal class VariantEvidence private constructor(private val actionableHotspots: List<ActionableHotspot>,
                                                   private val actionableRanges: List<ActionableRange>, private val applicableActionableGenes: List<ActionableGene>) : EvidenceMatcher<PurpleVariant> {
    override fun findMatches(variant: PurpleVariant): MutableList<ActionableEvent> {
        return Stream.of(hotspotMatches(variant), rangeMatches(variant), geneMatches(variant))
            .flatMap { obj: List<ActionableEvent> -> obj.stream() }
            .collect(Collectors.toList())
    }

    private fun hotspotMatches(variant: PurpleVariant): List<ActionableEvent> {
        return filterMatchingEvents(actionableHotspots, variant) { obj: ActionableHotspot, hotspot: PurpleVariant -> HotspotMatching.isMatch(obj, hotspot) }
    }

    private fun rangeMatches(variant: PurpleVariant): MutableList<ActionableEvent> {
        return filterMatchingEvents(actionableRanges, variant) { obj: ActionableRange, rangeAnnotation: PurpleVariant -> RangeMatching.isMatch(obj, rangeAnnotation) }
    }

    private fun geneMatches(variant: PurpleVariant): MutableList<ActionableEvent> {
        return filterMatchingEvents<ActionableGene>(applicableActionableGenes, variant) { obj: ActionableGene, geneAnnotation: PurpleVariant -> GeneMatching.isMatch(obj, geneAnnotation) }
    }

    private fun <T : ActionableEvent> filterMatchingEvents(events: List<T>, variant: PurpleVariant,
                                                           predicate: BiPredicate<T, PurpleVariant>): MutableList<ActionableEvent> {
        return if (!variant.reported()) mutableListOf() else events.stream().filter { event: T -> predicate.test(event, variant) }.collect(Collectors.toList())
    }

    companion object {
        private val APPLICABLE_GENE_EVENTS = Set.of(GeneEvent.ACTIVATION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION)
        fun create(actionableEvents: ActionableEvents): VariantEvidence {
            val applicableActionableGenes = actionableEvents.genes()
                .stream()
                .filter { actionableGene: ActionableGene -> APPLICABLE_GENE_EVENTS.contains(actionableGene.event()) }
                .collect(Collectors.toList())
            val ranges = Stream.of(actionableEvents.codons(), actionableEvents.exons()).flatMap { obj: MutableList<ActionableRange> -> obj.stream() }.collect(Collectors.toList())
            return VariantEvidence(actionableEvents.hotspots(), ranges, applicableActionableGenes)
        }
    }
}