package com.hartwig.actin.molecular.orange.evidence.known;

import java.util.List;

import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.serve.KnownGene;
import com.hartwig.serve.datamodel.KnownEvents;
import com.hartwig.serve.datamodel.common.GeneAlteration;
import com.hartwig.serve.datamodel.fusion.KnownFusion;
import com.hartwig.serve.datamodel.gene.KnownCopyNumber;
import com.hartwig.serve.datamodel.hotspot.KnownHotspot;
import com.hartwig.serve.datamodel.range.KnownCodon;
import com.hartwig.serve.datamodel.range.KnownExon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KnownEventResolver {

    @NotNull
    private final KnownEvents knownEvents;
    @NotNull
    private final List<KnownGene> knownGenes;

    public KnownEventResolver(@NotNull final KnownEvents knownEvents, @NotNull final List<KnownGene> knownGenes) {
        this.knownEvents = knownEvents;
        this.knownGenes = knownGenes;
    }

    @Nullable
    public GeneAlteration resolveForVariant(@NotNull PurpleVariant variant) {
        KnownHotspot hotspot = HotspotLookup.find(knownEvents.hotspots(), variant);
        if (hotspot != null) {
            return hotspot;
        }

        KnownCodon codon = CodonLookup.find(knownEvents.codons(), variant);
        if (codon != null) {
            return codon;
        }

        KnownExon exon =  ExonLookup.find(knownEvents.exons(), variant);
        if (exon != null) {
            return exon;
        }

        return GeneLookup.find(knownGenes, variant.gene());
    }

    @Nullable
    public GeneAlteration resolveForCopyNumber(@NotNull PurpleCopyNumber copyNumber) {
        KnownCopyNumber knownCopyNumber = CopyNumberLookup.findForCopyNumber(knownEvents.copyNumbers(), copyNumber);
        if (knownCopyNumber != null) {
            return knownCopyNumber;
        }

        return GeneLookup.find(knownGenes, copyNumber.gene());
    }

    @Nullable
    public GeneAlteration resolveForHomozygousDisruption(@NotNull LinxHomozygousDisruption homozygousDisruption) {
        // Assume a homozygous disruption always has the same annotation as a loss.
        KnownCopyNumber knownCopyNumber =  CopyNumberLookup.findForHomozygousDisruption(knownEvents.copyNumbers(), homozygousDisruption);
        if (knownCopyNumber != null) {
            return knownCopyNumber;
        }

        return GeneLookup.find(knownGenes, homozygousDisruption.gene());
    }

    @Nullable
    public GeneAlteration resolveForDisruption(@NotNull LinxDisruption disruption) {
        return GeneLookup.find(knownGenes, disruption.gene());
    }

    @Nullable
    public KnownFusion resolveForFusion(@NotNull LinxFusion fusion) {
        return FusionLookup.find(knownEvents.fusions(), fusion);
    }
}
