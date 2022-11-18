package com.hartwig.actin.molecular.orange.evidence;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.orange.curation.ExternalTrialMapping;
import com.hartwig.actin.molecular.orange.evidence.actionable.ActionableEventMatcher;
import com.hartwig.actin.molecular.orange.evidence.known.KnownEventResolver;
import com.hartwig.actin.molecular.serve.KnownGene;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.ImmutableActionableEvents;
import com.hartwig.serve.datamodel.ImmutableKnownEvents;
import com.hartwig.serve.datamodel.Knowledgebase;
import com.hartwig.serve.datamodel.KnownEvent;
import com.hartwig.serve.datamodel.KnownEvents;

import org.jetbrains.annotations.NotNull;

public final class EvidenceDatabaseFactory {

    private static final Set<Knowledgebase> KNOWN_EVENT_SOURCES = Sets.newHashSet(Knowledgebase.CKB);
    private static final Set<Knowledgebase> ACTIONABLE_EVENT_SOURCES = Sets.newHashSet(Knowledgebase.CKB, Knowledgebase.ICLUSION);

    private EvidenceDatabaseFactory() {
    }

    @NotNull
    public static EvidenceDatabase create(@NotNull KnownEvents knownEvents, @NotNull List<KnownGene> knownGenes,
            @NotNull ActionableEvents actionableEvents, @NotNull List<ExternalTrialMapping> externalTrialMappings) {
        KnownEventResolver knownEventResolver = new KnownEventResolver(filterKnownEvents(knownEvents, KNOWN_EVENT_SOURCES), knownGenes);
        ActionableEventMatcher actionableEventMatcher =
                new ActionableEventMatcher(filterActionableEvents(actionableEvents, ACTIONABLE_EVENT_SOURCES), externalTrialMappings);
        return new EvidenceDatabase(knownEventResolver, actionableEventMatcher);
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

    @NotNull
    private static ActionableEvents filterActionableEvents(@NotNull ActionableEvents actionableEvents,
            @NotNull Set<Knowledgebase> sourcesToInclude) {
        return ImmutableActionableEvents.builder()
                .hotspots(filterActionable(actionableEvents.hotspots(), sourcesToInclude))
                .ranges(filterActionable(actionableEvents.ranges(), sourcesToInclude))
                .genes(filterActionable(actionableEvents.genes(), sourcesToInclude))
                .fusions(filterActionable(actionableEvents.fusions(), sourcesToInclude))
                .characteristics(filterActionable(actionableEvents.characteristics(), sourcesToInclude))
                .hla(filterActionable(actionableEvents.hla(), sourcesToInclude))
                .build();
    }

    @NotNull
    private static <T extends ActionableEvent> Set<T> filterActionable(@NotNull List<T> actionables,
            @NotNull Set<Knowledgebase> sourcesToInclude) {
        Set<T> filtered = Sets.newHashSet();
        for (T actionable : actionables) {
            if (sourcesToInclude.contains(actionable.source())) {
                filtered.add(actionable);
            }
        }
        return filtered;
    }
}
