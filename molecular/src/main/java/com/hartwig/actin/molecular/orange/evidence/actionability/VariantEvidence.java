package com.hartwig.actin.molecular.orange.evidence.actionability;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.evidence.matching.GeneMatching;
import com.hartwig.actin.molecular.orange.evidence.matching.HotspotMatching;
import com.hartwig.actin.molecular.orange.evidence.matching.RangeMatching;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.gene.ActionableGene;
import com.hartwig.serve.datamodel.gene.GeneEvent;
import com.hartwig.serve.datamodel.hotspot.ActionableHotspot;
import com.hartwig.serve.datamodel.range.ActionableRange;

import org.jetbrains.annotations.NotNull;

class VariantEvidence {

    private static final Set<GeneEvent> APPLICABLE_GENE_EVENTS =
            Sets.newHashSet(GeneEvent.ACTIVATION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION);

    @NotNull
    private final List<ActionableHotspot> actionableHotspots;
    @NotNull
    private final List<ActionableRange> actionableRanges;
    @NotNull
    private final List<ActionableGene> applicableActionableGenes;

    @NotNull
    public static VariantEvidence create(@NotNull ActionableEvents actionableEvents) {
        List<ActionableGene> applicableActionableGenes = Lists.newArrayList();
        for (ActionableGene actionableGene : actionableEvents.genes()) {
            if (APPLICABLE_GENE_EVENTS.contains(actionableGene.event())) {
                applicableActionableGenes.add(actionableGene);
            }
        }

        return new VariantEvidence(actionableEvents.hotspots(), actionableEvents.ranges(), applicableActionableGenes);
    }

    private VariantEvidence(@NotNull final List<ActionableHotspot> actionableHotspots,
            @NotNull final List<ActionableRange> actionableRanges, @NotNull final List<ActionableGene> applicableActionableGenes) {
        this.actionableHotspots = actionableHotspots;
        this.actionableRanges = actionableRanges;
        this.applicableActionableGenes = applicableActionableGenes;
    }

    @NotNull
    public List<ActionableEvent> findMatches(@NotNull PurpleVariant variant) {
        List<ActionableEvent> applicableEvents = Lists.newArrayList();

        applicableEvents.addAll(hotspotMatches(variant));
        applicableEvents.addAll(rangeMatches(variant));
        applicableEvents.addAll(geneMatches(variant));

        return applicableEvents;
    }

    @NotNull
    private List<ActionableEvent> hotspotMatches(@NotNull PurpleVariant variant) {
        List<ActionableEvent> matches = Lists.newArrayList();
        for (ActionableHotspot actionableHotspot : actionableHotspots) {
            if (HotspotMatching.isMatch(actionableHotspot, variant)) {
                matches.add(actionableHotspot);
            }
        }
        return matches;
    }

    @NotNull
    private List<ActionableEvent> rangeMatches(@NotNull PurpleVariant variant) {
        List<ActionableEvent> matches = Lists.newArrayList();
        for (ActionableRange actionableRange : actionableRanges) {
            if (RangeMatching.isMatch(actionableRange, variant)) {
                matches.add(actionableRange);
            }
        }
        return matches;
    }

    @NotNull
    private List<ActionableEvent> geneMatches(@NotNull PurpleVariant variant) {
        List<ActionableEvent> matches = Lists.newArrayList();
        for (ActionableGene actionableGene : applicableActionableGenes) {
            if (GeneMatching.isMatch(actionableGene, variant)) {
                matches.add(actionableGene);
            }
        }
        return matches;
    }
}
