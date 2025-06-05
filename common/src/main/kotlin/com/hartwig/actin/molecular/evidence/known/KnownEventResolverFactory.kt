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
        val primaryKnownEvents = includeKnownEvents(knownEvents, includePrimaryEvents = true)
        val secondaryKnownEvents = includeKnownEvents(knownEvents, includePrimaryEvents = false)
        return KnownEventResolver(primaryKnownEvents, secondaryKnownEvents, GeneAggregator.aggregate(knownEvents.genes()))
    }

    fun includeKnownEvents(knownEvents: KnownEvents, includePrimaryEvents: Boolean): KnownEvents {
        return ImmutableKnownEvents.builder()
            .hotspots(filterKnown<KnownHotspot>(knownEvents.hotspots(), includePrimaryEvents))
            .codons(filterKnown<KnownCodon>(knownEvents.codons(), includePrimaryEvents))
            .exons(filterKnown<KnownExon>(knownEvents.exons(), includePrimaryEvents))
            .genes(filterKnown<KnownGene>(knownEvents.genes(), includePrimaryEvents))
            .copyNumbers(filterKnown<KnownCopyNumber>(knownEvents.copyNumbers(), includePrimaryEvents))
            .fusions(filterKnown<KnownFusion>(knownEvents.fusions(), includePrimaryEvents))
            .build()
    }

    private fun <T : KnownEvent> filterKnown(knowns: Set<T>, includePrimaryEvents: Boolean): Set<T> {
        return knowns.filter { it.sources().contains(PRIMARY_KNOWN_EVENT_SOURCE) == includePrimaryEvents }.toSet()
    }
}
