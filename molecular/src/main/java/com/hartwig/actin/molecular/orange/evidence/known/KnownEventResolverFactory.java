package com.hartwig.actin.molecular.orange.evidence.known;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityConstants;
import com.hartwig.actin.molecular.serve.KnownGene;
import com.hartwig.serve.datamodel.ImmutableKnownEvents;
import com.hartwig.serve.datamodel.Knowledgebase;
import com.hartwig.serve.datamodel.KnownEvent;
import com.hartwig.serve.datamodel.KnownEvents;

import org.jetbrains.annotations.NotNull;

public final class KnownEventResolverFactory {

    private static final Set<Knowledgebase> KNOWN_EVENT_SOURCES = Sets.newHashSet(ActionabilityConstants.EVIDENCE_SOURCE);

    private KnownEventResolverFactory() {
    }

    @NotNull
    public static KnownEventResolver create(@NotNull KnownEvents knownEvents, @NotNull List<KnownGene> knownGenes) {
        return new KnownEventResolver(filterKnownEvents(knownEvents, KNOWN_EVENT_SOURCES), knownGenes);
    }

    @NotNull
    private static KnownEvents filterKnownEvents(@NotNull KnownEvents knownEvents, @NotNull Set<Knowledgebase> sourcesToInclude) {
        return ImmutableKnownEvents.builder()
                .hotspots(filterKnown(knownEvents.hotspots(), sourcesToInclude))
                .codons(filterKnown(knownEvents.codons(), sourcesToInclude))
                .exons(filterKnown(knownEvents.exons(), sourcesToInclude))
                .copyNumbers(filterKnown(knownEvents.copyNumbers(), sourcesToInclude))
                .fusions(filterKnown(knownEvents.fusions(), sourcesToInclude))
                .build();
    }

    @NotNull
    private static <T extends KnownEvent> Set<T> filterKnown(@NotNull Set<T> knowns, @NotNull Set<Knowledgebase> sourcesToInclude) {
        Set<T> filtered = Sets.newHashSet();
        for (T known : knowns) {
            if (hasAtLeastOneSourceToInclude(known.sources(), sourcesToInclude)) {
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
