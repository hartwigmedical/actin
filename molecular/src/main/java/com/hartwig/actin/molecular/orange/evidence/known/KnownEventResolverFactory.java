package com.hartwig.actin.molecular.orange.evidence.known;

import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityConstants;
import com.hartwig.serve.datamodel.ImmutableKnownEvents;
import com.hartwig.serve.datamodel.Knowledgebase;
import com.hartwig.serve.datamodel.KnownEvent;
import com.hartwig.serve.datamodel.KnownEvents;

import org.jetbrains.annotations.NotNull;

public final class KnownEventResolverFactory {

    static final Set<Knowledgebase> KNOWN_EVENT_SOURCES = Sets.newHashSet(ActionabilityConstants.EVIDENCE_SOURCE);

    private KnownEventResolverFactory() {
    }

    @NotNull
    public static KnownEventResolver create(@NotNull KnownEvents knownEvents) {
        return new KnownEventResolver(filterKnownEvents(knownEvents), GeneAggregator.aggregate(knownEvents.genes()));
    }

    @NotNull
    @VisibleForTesting
    static KnownEvents filterKnownEvents(@NotNull KnownEvents knownEvents) {
        return ImmutableKnownEvents.builder()
                .hotspots(filterKnown(knownEvents.hotspots()))
                .codons(filterKnown(knownEvents.codons()))
                .exons(filterKnown(knownEvents.exons()))
                .genes(filterKnown(knownEvents.genes()))
                .copyNumbers(filterKnown(knownEvents.copyNumbers()))
                .fusions(filterKnown(knownEvents.fusions()))
                .build();
    }

    @NotNull
    private static <T extends KnownEvent> Set<T> filterKnown(@NotNull Set<T> knowns) {
        Set<T> filtered = Sets.newHashSet();
        for (T known : knowns) {
            if (hasAtLeastOneSourceToInclude(known.sources(), KNOWN_EVENT_SOURCES)) {
                filtered.add(known);
            }
        }
        return filtered;
    }

    private static boolean hasAtLeastOneSourceToInclude(@NotNull Set<Knowledgebase> sources, @NotNull Set<Knowledgebase> sourcesToInclude) {
        for (Knowledgebase source : sources) {
            if (sourcesToInclude.contains(source)) {
                return true;
            }
        }
        return false;
    }
}
