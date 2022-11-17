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
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.KnownEvents;
import com.hartwig.serve.datamodel.common.GeneAlteration;
import com.hartwig.serve.datamodel.fusion.KnownFusion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvidenceDatabase {

    @NotNull
    private final KnownEvents knownEvents;
    @NotNull
    private final ActionableEvents actionableEvents;
    @NotNull
    private final List<ExternalTrialMapping> externalTrialMappings;

    public EvidenceDatabase(@NotNull final KnownEvents knownEvents, @NotNull final ActionableEvents actionableEvents,
            @NotNull final List<ExternalTrialMapping> externalTrialMappings) {
        this.knownEvents = knownEvents;
        this.actionableEvents = actionableEvents;
        this.externalTrialMappings = externalTrialMappings;
    }

    @Nullable
    public GeneAlteration lookupGeneAlteration(@NotNull PurpleVariant variant) {
        return null;
    }

    @NotNull
    public List<ActionableEvent> lookUpActionableEvents(@NotNull PurpleVariant variant) {
        return Lists.newArrayList();
    }

    @Nullable
    public GeneAlteration lookupGeneAlteration(@NotNull PurpleCopyNumber copyNumber) {
        return null;
    }

    @NotNull
    public List<ActionableEvent> lookUpActionableEvents(@NotNull PurpleCopyNumber copyNumber) {
        return Lists.newArrayList();
    }

    @Nullable
    public GeneAlteration lookupGeneAlteration(@NotNull LinxHomozygousDisruption homozygousDisruption) {
        return null;
    }

    @NotNull
    public List<ActionableEvent> lookUpActionableEvents(@NotNull LinxHomozygousDisruption homozygousDisruption) {
        return Lists.newArrayList();
    }

    @Nullable
    public GeneAlteration lookupGeneAlteration(@NotNull LinxDisruption disruption) {
        return null;
    }

    @NotNull
    public List<ActionableEvent> lookUpActionableEvents(@NotNull LinxDisruption disruption) {
        return Lists.newArrayList();
    }

    @Nullable
    public KnownFusion lookupKnownFusion(@NotNull LinxFusion fusion) {
        return null;
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
