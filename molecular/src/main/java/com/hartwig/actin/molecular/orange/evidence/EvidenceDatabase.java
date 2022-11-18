package com.hartwig.actin.molecular.orange.evidence;

import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.actin.molecular.orange.evidence.actionable.ActionabilityMatch;
import com.hartwig.actin.molecular.orange.evidence.actionable.ActionableEventResolver;
import com.hartwig.actin.molecular.orange.evidence.known.KnownEventResolver;
import com.hartwig.serve.datamodel.common.GeneAlteration;
import com.hartwig.serve.datamodel.fusion.KnownFusion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvidenceDatabase {

    @NotNull
    private final KnownEventResolver knownEventResolver;
    @NotNull
    private final ActionableEventResolver actionableEventResolver;

    EvidenceDatabase(@NotNull final KnownEventResolver knownEventResolver,
            @NotNull final ActionableEventResolver actionableEventResolver) {
        this.knownEventResolver = knownEventResolver;
        this.actionableEventResolver = actionableEventResolver;
    }

    @Nullable
    public GeneAlteration lookupGeneAlteration(@NotNull PurpleVariant variant) {
        return knownEventResolver.resolveForVariant(variant);
    }

    @NotNull
    public ActionabilityMatch matchToActionableEvidence(@NotNull PurpleVariant variant) {
        return actionableEventResolver.resolveForVariant(variant);
    }

    @Nullable
    public GeneAlteration lookupGeneAlteration(@NotNull PurpleCopyNumber copyNumber) {
        return knownEventResolver.resolveForCopyNumber(copyNumber);
    }

    @NotNull
    public ActionabilityMatch matchToActionableEvidence(@NotNull PurpleCopyNumber copyNumber) {
        return actionableEventResolver.resolveForCopyNumber(copyNumber);
    }

    @Nullable
    public GeneAlteration lookupGeneAlteration(@NotNull LinxHomozygousDisruption homozygousDisruption) {
        return knownEventResolver.resolveForHomozygousDisruption(homozygousDisruption);
    }

    @NotNull
    public ActionabilityMatch matchToActionableEvidence(@NotNull LinxHomozygousDisruption homozygousDisruption) {
        return actionableEventResolver.resolveForHomozygousDisruption(homozygousDisruption);
    }

    @Nullable
    public GeneAlteration lookupGeneAlteration(@NotNull LinxDisruption disruption) {
        return knownEventResolver.resolveForDisruption(disruption);
    }

    @NotNull
    public ActionabilityMatch matchToActionableEvidence(@NotNull LinxDisruption disruption) {
        return actionableEventResolver.resolveForDisruption(disruption);
    }

    @Nullable
    public KnownFusion lookupKnownFusion(@NotNull LinxFusion fusion) {
        return knownEventResolver.resolveForFusion(fusion);
    }

    @NotNull
    public ActionabilityMatch matchToActionableEvidence(@NotNull LinxFusion fusion) {
        return actionableEventResolver.resolveForFusion(fusion);
    }

    @NotNull
    public ActionabilityMatch matchToActionableEvidence(@NotNull VirusInterpreterEntry virus) {
        return actionableEventResolver.resolveForVirus(virus);
    }
}
