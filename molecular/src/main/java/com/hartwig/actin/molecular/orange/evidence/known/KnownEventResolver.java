package com.hartwig.actin.molecular.orange.evidence.known;

import java.util.Set;

import com.hartwig.actin.molecular.orange.datamodel.linx.LinxBreakend;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.evidence.matching.HotspotMatching;
import com.hartwig.actin.molecular.orange.evidence.matching.RangeMatching;
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss;
import com.hartwig.hmftools.datamodel.purple.PurpleVariant;
import com.hartwig.serve.datamodel.KnownEvents;
import com.hartwig.serve.datamodel.common.GeneAlteration;
import com.hartwig.serve.datamodel.fusion.KnownFusion;
import com.hartwig.serve.datamodel.gene.KnownCopyNumber;
import com.hartwig.serve.datamodel.gene.KnownGene;
import com.hartwig.serve.datamodel.hotspot.KnownHotspot;
import com.hartwig.serve.datamodel.range.KnownCodon;
import com.hartwig.serve.datamodel.range.KnownExon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KnownEventResolver {

    @NotNull
    private final KnownEvents knownEvents;
    @NotNull
    private final Set<KnownGene> aggregatedKnownGenes;

    KnownEventResolver(@NotNull final KnownEvents knownEvents, @NotNull Set<KnownGene> aggregatedKnownGenes) {
        this.knownEvents = knownEvents;
        this.aggregatedKnownGenes = aggregatedKnownGenes;
    }

    @Nullable
    public GeneAlteration resolveForVariant(@NotNull PurpleVariant variant) {
        KnownHotspot hotspot = findHotspot(knownEvents.hotspots(), variant);
        if (hotspot != null) {
            return hotspot;
        }

        KnownCodon codon = findCodon(knownEvents.codons(), variant);
        if (codon != null) {
            return codon;
        }

        KnownExon exon = findExon(knownEvents.exons(), variant);
        if (exon != null) {
            return exon;
        }

        return GeneLookup.find(aggregatedKnownGenes, variant.gene());
    }

    @Nullable
    private static KnownHotspot findHotspot(@NotNull Iterable<KnownHotspot> knownHotspots, @NotNull PurpleVariant variant) {
        for (KnownHotspot knownHotspot : knownHotspots) {
            if (HotspotMatching.isMatch(knownHotspot, variant)) {
                return knownHotspot;
            }
        }

        return null;
    }

    @Nullable
    private static KnownCodon findCodon(@NotNull Iterable<KnownCodon> knownCodons, @NotNull PurpleVariant variant) {
        for (KnownCodon knownCodon : knownCodons) {
            if (RangeMatching.isMatch(knownCodon, variant)) {
                return knownCodon;
            }
        }
        return null;
    }

    @Nullable
    private static KnownExon findExon(@NotNull Iterable<KnownExon> knownExons, @NotNull PurpleVariant variant) {
        for (KnownExon knownExon : knownExons) {
            if (RangeMatching.isMatch(knownExon, variant)) {
                return knownExon;
            }
        }
        return null;
    }

    @Nullable
    public GeneAlteration resolveForCopyNumber(@NotNull PurpleGainLoss gainLoss) {
        KnownCopyNumber knownCopyNumber = CopyNumberLookup.findForCopyNumber(knownEvents.copyNumbers(), gainLoss);
        if (knownCopyNumber != null) {
            return knownCopyNumber;
        }

        return GeneLookup.find(aggregatedKnownGenes, gainLoss.gene());
    }

    @Nullable
    public GeneAlteration resolveForHomozygousDisruption(@NotNull LinxHomozygousDisruption homozygousDisruption) {
        // Assume a homozygous disruption always has the same annotation as a loss.
        KnownCopyNumber knownCopyNumber = CopyNumberLookup.findForHomozygousDisruption(knownEvents.copyNumbers(), homozygousDisruption);
        if (knownCopyNumber != null) {
            return knownCopyNumber;
        }

        return GeneLookup.find(aggregatedKnownGenes, homozygousDisruption.gene());
    }

    @Nullable
    public GeneAlteration resolveForBreakend(@NotNull LinxBreakend breakend) {
        return GeneLookup.find(aggregatedKnownGenes, breakend.gene());
    }

    @Nullable
    public KnownFusion resolveForFusion(@NotNull LinxFusion fusion) {
        return FusionLookup.find(knownEvents.fusions(), fusion);
    }
}
