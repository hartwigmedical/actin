package com.hartwig.actin.molecular.orange.evidence;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.curation.ExternalTrialMapping;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.actin.molecular.orange.evidence.known.CodonLookup;
import com.hartwig.actin.molecular.orange.evidence.known.CopyNumberLookup;
import com.hartwig.actin.molecular.orange.evidence.known.ExonLookup;
import com.hartwig.actin.molecular.orange.evidence.known.FusionLookup;
import com.hartwig.actin.molecular.orange.evidence.known.GeneLookup;
import com.hartwig.actin.molecular.orange.evidence.known.HotspotLookup;
import com.hartwig.actin.molecular.serve.KnownGene;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.KnownEvents;
import com.hartwig.serve.datamodel.common.GeneAlteration;
import com.hartwig.serve.datamodel.fusion.KnownFusion;
import com.hartwig.serve.datamodel.gene.KnownCopyNumber;
import com.hartwig.serve.datamodel.hotspot.KnownHotspot;
import com.hartwig.serve.datamodel.range.KnownCodon;
import com.hartwig.serve.datamodel.range.KnownExon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvidenceDatabase {

    @NotNull
    private final KnownEvents knownEvents;
    @NotNull
    private final List<KnownGene> knownGenes;
    @NotNull
    private final ActionableEvents actionableEvents;
    @NotNull
    private final List<ExternalTrialMapping> externalTrialMappings;

    EvidenceDatabase(@NotNull final KnownEvents knownEvents, @NotNull final List<KnownGene> knownGenes,
            @NotNull final ActionableEvents actionableEvents, @NotNull final List<ExternalTrialMapping> externalTrialMappings) {
        this.knownEvents = knownEvents;
        this.knownGenes = knownGenes;
        this.actionableEvents = actionableEvents;
        this.externalTrialMappings = externalTrialMappings;
    }

    @Nullable
    public GeneAlteration lookupGeneAlteration(@NotNull PurpleVariant variant) {
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

    @NotNull
    public List<ActionableEvent> lookUpActionableEvents(@NotNull PurpleVariant variant) {
        return Lists.newArrayList();
    }

    @Nullable
    public GeneAlteration lookupGeneAlteration(@NotNull PurpleCopyNumber copyNumber) {
        KnownCopyNumber knownCopyNumber = CopyNumberLookup.findForCopyNumber(knownEvents.copyNumbers(), copyNumber);
        if (knownCopyNumber != null) {
            return knownCopyNumber;
        }

        return GeneLookup.find(knownGenes, copyNumber.gene());
    }

    @NotNull
    public List<ActionableEvent> lookUpActionableEvents(@NotNull PurpleCopyNumber copyNumber) {
        return Lists.newArrayList();
    }

    @Nullable
    public GeneAlteration lookupGeneAlteration(@NotNull LinxHomozygousDisruption homozygousDisruption) {
        // Assume a homozygous disruption always has the same annotation as a loss.
        KnownCopyNumber knownCopyNumber =  CopyNumberLookup.findForHomozygousDisruption(knownEvents.copyNumbers(), homozygousDisruption);
        if (knownCopyNumber != null) {
            return knownCopyNumber;
        }

        return GeneLookup.find(knownGenes, homozygousDisruption.gene());
    }

    @NotNull
    public List<ActionableEvent> lookUpActionableEvents(@NotNull LinxHomozygousDisruption homozygousDisruption) {
        return Lists.newArrayList();
    }

    @Nullable
    public GeneAlteration lookupGeneAlteration(@NotNull LinxDisruption disruption) {
        return GeneLookup.find(knownGenes, disruption.gene());
    }

    @NotNull
    public List<ActionableEvent> lookUpActionableEvents(@NotNull LinxDisruption disruption) {
        return Lists.newArrayList();
    }

    @Nullable
    public KnownFusion lookupKnownFusion(@NotNull LinxFusion fusion) {
        return FusionLookup.find(knownEvents.fusions(), fusion);
    }

    @NotNull
    public List<ActionableEvent> lookUpActionableEvents(@NotNull LinxFusion fusion) {
        return Lists.newArrayList();
    }

    @NotNull
    public List<ActionableEvent> lookUpActionableEvents(@NotNull VirusInterpreterEntry virus) {
        return Lists.newArrayList();
    }
}
