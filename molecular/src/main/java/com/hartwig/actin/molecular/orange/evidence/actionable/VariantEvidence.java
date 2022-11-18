package com.hartwig.actin.molecular.orange.evidence.actionable;

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

final class VariantEvidence {

    //    this.genes = genes.stream()
    //            .filter(x -> x.event() == GeneLevelEvent.ACTIVATION || x.event() == GeneLevelEvent.INACTIVATION
    //                        || x.event() == GeneLevelEvent.ANY_MUTATION)
    //            .collect(Collectors.toList());
    //
    private static final Set<GeneEvent> APPLICABLE_GENE_EVENTS =
            Sets.newHashSet(GeneEvent.ACTIVATION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION);

    private VariantEvidence() {
    }

    @NotNull
    public static List<ActionableEvent> findMatches(@NotNull ActionableEvents actionableEvents, @NotNull PurpleVariant variant) {
        List<ActionableEvent> applicableEvents = Lists.newArrayList();

        applicableEvents.addAll(hotspotMatches(actionableEvents.hotspots(), variant));
        applicableEvents.addAll(rangeMatches(actionableEvents.ranges(), variant));
        applicableEvents.addAll(geneMatches(actionableEvents.genes(), variant));

        return applicableEvents;
    }

    @NotNull
    private static List<ActionableEvent> hotspotMatches(@NotNull List<ActionableHotspot> hotspots, @NotNull PurpleVariant variant) {
        List<ActionableEvent> matches = Lists.newArrayList();
        for (ActionableHotspot hotspot : hotspots) {
            if (HotspotMatching.isMatch(hotspot, variant)) {
                matches.add(hotspot);
            }
        }
        return matches;
    }

    @NotNull
    private static List<ActionableEvent> rangeMatches(@NotNull List<ActionableRange> ranges, @NotNull PurpleVariant variant) {
        List<ActionableEvent> matches = Lists.newArrayList();
        for (ActionableRange range : ranges) {
            if (RangeMatching.isMatch(range, variant)) {
                matches.add(range);
            }
        }
        return matches;
    }

    @NotNull
    private static List<ActionableEvent> geneMatches(@NotNull List<ActionableGene> genes, @NotNull PurpleVariant variant) {
        List<ActionableEvent> matches = Lists.newArrayList();
        for (ActionableGene gene : genes) {
            if (APPLICABLE_GENE_EVENTS.contains(gene.event()) && GeneMatching.isMatch(gene, variant)) {
                matches.add(gene);
            }
        }
        return matches;
    }
}
