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
        val primaryKnownEvents = filterKnownEvents(knownEvents, filterPrimaryEvents = true)
        val secondaryKnownEvents = filterKnownEvents(knownEvents, filterPrimaryEvents = false)
        return KnownEventResolver(primaryKnownEvents, secondaryKnownEvents, GeneAggregator.aggregate(knownEvents.genes()))
    }

    fun filterKnownEvents(
        knownEvents: KnownEvents,
        filterPrimaryEvents: Boolean
    ): KnownEvents {
        return ImmutableKnownEvents.builder()
            .hotspots(filterKnown<KnownHotspot>(knownEvents.hotspots(), filterPrimaryEvents))
            .codons(filterKnown<KnownCodon>(knownEvents.codons(), filterPrimaryEvents))
            .exons(filterKnown<KnownExon>(knownEvents.exons(), filterPrimaryEvents))
            .genes(filterKnown<KnownGene>(knownEvents.genes(), filterPrimaryEvents))
            .copyNumbers(filterKnown<KnownCopyNumber>(knownEvents.copyNumbers(), filterPrimaryEvents))
            .fusions(filterKnown<KnownFusion>(knownEvents.fusions(), filterPrimaryEvents))
            .build()
    }

    private fun <T : KnownEvent> filterKnown(knowns: Set<T>, filterPrimaryEvents: Boolean): Set<T> {
        return knowns.filter { it.sources().contains(PRIMARY_KNOWN_EVENT_SOURCE) == filterPrimaryEvents }.toSet()
    }
}
