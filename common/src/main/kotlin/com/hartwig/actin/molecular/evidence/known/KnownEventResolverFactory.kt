package com.hartwig.actin.molecular.evidence.known

import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.serve.datamodel.molecular.ImmutableKnownEvents
import com.hartwig.serve.datamodel.molecular.KnownEvent
import com.hartwig.serve.datamodel.molecular.KnownEvents
import com.hartwig.serve.datamodel.molecular.fusion.KnownFusion
import com.hartwig.serve.datamodel.molecular.gene.KnownCopyNumber
import com.hartwig.serve.datamodel.molecular.gene.KnownGene
import com.hartwig.serve.datamodel.molecular.hotspot.KnownHotspot
import com.hartwig.serve.datamodel.molecular.range.KnownCodon
import com.hartwig.serve.datamodel.molecular.range.KnownExon

object KnownEventResolverFactory {

    val KNOWN_EVENT_SOURCES = setOf(ActionabilityConstants.EVIDENCE_SOURCE)

    fun create(knownEvents: KnownEvents): KnownEventResolver {
        return KnownEventResolver(knownEvents, filterKnownEvents(knownEvents), GeneAggregator.aggregate(knownEvents.genes()))
    }

    fun filterKnownEvents(knownEvents: KnownEvents): KnownEvents {
        return ImmutableKnownEvents.builder()
            .hotspots(filterKnown<KnownHotspot>(knownEvents.hotspots()))
            .codons(filterKnown<KnownCodon>(knownEvents.codons()))
            .exons(filterKnown<KnownExon>(knownEvents.exons()))
            .genes(filterKnown<KnownGene>(knownEvents.genes()))
            .copyNumbers(filterKnown<KnownCopyNumber>(knownEvents.copyNumbers()))
            .fusions(filterKnown<KnownFusion>(knownEvents.fusions()))
            .build()
    }

    private fun <T : KnownEvent> filterKnown(knowns: Set<T>): Set<T> {
        return knowns.filter { it.sources().intersect(KNOWN_EVENT_SOURCES).isNotEmpty() }.toSet()
    }
}
