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

    val PRIMARY_KNOWN_EVENT_SOURCE = ActionabilityConstants.EVIDENCE_SOURCE

    fun create(knownEvents: KnownEvents): KnownEventResolver {
        val primaryKnownEvents = filterKnownEvents(knownEvents, isPrimary = true)
        val secondaryKnownEvents = filterKnownEvents(knownEvents, isPrimary = false)
        return KnownEventResolver(primaryKnownEvents, secondaryKnownEvents, GeneAggregator.aggregate(knownEvents.genes()))
    }

    fun filterKnownEvents(
        knownEvents: KnownEvents,
        isPrimary: Boolean
    ): KnownEvents {
        return ImmutableKnownEvents.builder()
            .hotspots(filterKnown<KnownHotspot>(knownEvents.hotspots(), isPrimary))
            .codons(filterKnown<KnownCodon>(knownEvents.codons(), isPrimary))
            .exons(filterKnown<KnownExon>(knownEvents.exons(), isPrimary))
            .genes(filterKnown<KnownGene>(knownEvents.genes(), isPrimary))
            .copyNumbers(filterKnown<KnownCopyNumber>(knownEvents.copyNumbers(), isPrimary))
            .fusions(filterKnown<KnownFusion>(knownEvents.fusions(), isPrimary))
            .build()
    }

    private fun <T : KnownEvent> filterKnown(knowns: Set<T>, isPrimary: Boolean): Set<T> {
        return knowns.filter { it.sources().contains(PRIMARY_KNOWN_EVENT_SOURCE) == isPrimary }.toSet()
    }
}
