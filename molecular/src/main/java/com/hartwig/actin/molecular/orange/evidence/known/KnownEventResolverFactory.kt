package com.hartwig.actin.molecular.orange.evidence.known

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Sets
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityConstants
import com.hartwig.serve.datamodel.ImmutableKnownEvents
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.KnownEvent
import com.hartwig.serve.datamodel.KnownEvents
import com.hartwig.serve.datamodel.fusion.KnownFusion
import com.hartwig.serve.datamodel.gene.KnownCopyNumber
import com.hartwig.serve.datamodel.gene.KnownGene
import com.hartwig.serve.datamodel.hotspot.KnownHotspot
import com.hartwig.serve.datamodel.range.KnownCodon
import com.hartwig.serve.datamodel.range.KnownExon

object KnownEventResolverFactory {
    val KNOWN_EVENT_SOURCES: MutableSet<Knowledgebase> = Sets.newHashSet(ActionabilityConstants.EVIDENCE_SOURCE)
    fun create(knownEvents: KnownEvents): KnownEventResolver {
        return KnownEventResolver(filterKnownEvents(knownEvents), GeneAggregator.aggregate(knownEvents.genes()))
    }

    @VisibleForTesting
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

    private fun <T : KnownEvent> filterKnown(knowns: MutableSet<T>): MutableSet<T> {
        val filtered: MutableSet<T> = Sets.newHashSet()
        for (known in knowns) {
            if (hasAtLeastOneSourceToInclude(known.sources(), KNOWN_EVENT_SOURCES)) {
                filtered.add(known)
            }
        }
        return filtered
    }

    private fun hasAtLeastOneSourceToInclude(sources: MutableSet<Knowledgebase>, sourcesToInclude: MutableSet<Knowledgebase>): Boolean {
        for (source in sources) {
            if (sourcesToInclude.contains(source)) {
                return true
            }
        }
        return false
    }
}
