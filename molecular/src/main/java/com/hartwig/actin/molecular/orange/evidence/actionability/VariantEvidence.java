package com.hartwig.actin.molecular.orange.evidence.actionability;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.molecular.orange.evidence.matching.GeneMatching;
import com.hartwig.actin.molecular.orange.evidence.matching.HotspotMatching;
import com.hartwig.actin.molecular.orange.evidence.matching.RangeMatching;
import com.hartwig.hmftools.datamodel.purple.PurpleVariant;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.gene.ActionableGene;
import com.hartwig.serve.datamodel.gene.GeneEvent;
import com.hartwig.serve.datamodel.hotspot.ActionableHotspot;
import com.hartwig.serve.datamodel.range.ActionableRange;

import org.jetbrains.annotations.NotNull;

class VariantEvidence implements EvidenceMatcher<PurpleVariant> {

    private static final Set<GeneEvent> APPLICABLE_GENE_EVENTS =
            Set.of(GeneEvent.ACTIVATION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION);

    @NotNull
    private final List<ActionableHotspot> actionableHotspots;
    @NotNull
    private final List<ActionableRange> actionableRanges;
    @NotNull
    private final List<ActionableGene> applicableActionableGenes;

    @NotNull
    public static VariantEvidence create(@NotNull ActionableEvents actionableEvents) {
        List<ActionableGene> applicableActionableGenes = actionableEvents.genes()
                .stream()
                .filter(actionableGene -> APPLICABLE_GENE_EVENTS.contains(actionableGene.event()))
                .collect(Collectors.toList());

        List<ActionableRange> ranges =
                Stream.of(actionableEvents.codons(), actionableEvents.exons()).flatMap(Collection::stream).collect(Collectors.toList());

        return new VariantEvidence(actionableEvents.hotspots(), ranges, applicableActionableGenes);
    }

    private VariantEvidence(@NotNull final List<ActionableHotspot> actionableHotspots,
            @NotNull final List<ActionableRange> actionableRanges, @NotNull final List<ActionableGene> applicableActionableGenes) {
        this.actionableHotspots = actionableHotspots;
        this.actionableRanges = actionableRanges;
        this.applicableActionableGenes = applicableActionableGenes;
    }

    @NotNull
    @Override
    public List<ActionableEvent> findMatches(@NotNull PurpleVariant variant) {
        return Stream.of(hotspotMatches(variant), rangeMatches(variant), geneMatches(variant))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @NotNull
    private List<ActionableEvent> hotspotMatches(@NotNull PurpleVariant variant) {
        return filterMatchingEvents(actionableHotspots, variant, HotspotMatching::isMatch);
    }

    @NotNull
    private List<ActionableEvent> rangeMatches(@NotNull PurpleVariant variant) {
        return filterMatchingEvents(actionableRanges, variant, RangeMatching::isMatch);
    }

    @NotNull
    private List<ActionableEvent> geneMatches(@NotNull PurpleVariant variant) {
        return filterMatchingEvents(applicableActionableGenes, variant, GeneMatching::isMatch);
    }

    @NotNull
    private <T extends ActionableEvent> List<ActionableEvent> filterMatchingEvents(@NotNull List<T> events, @NotNull PurpleVariant variant,
            @NotNull BiPredicate<T, PurpleVariant> predicate) {
        return !variant.reported()
                ? Collections.emptyList()
                : events.stream().filter(event -> predicate.test(event, variant)).collect(Collectors.toList());
    }
}