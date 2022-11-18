package com.hartwig.actin.molecular.orange.evidence;

import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.actin.molecular.orange.evidence.actionable.ActionabilityMatch;
import com.hartwig.actin.molecular.orange.evidence.actionable.ActionableEventMatcher;
import com.hartwig.actin.molecular.orange.evidence.known.KnownEventResolver;
import com.hartwig.serve.datamodel.common.GeneAlteration;
import com.hartwig.serve.datamodel.fusion.KnownFusion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvidenceDatabase {

    @NotNull
    private final KnownEventResolver knownEventResolver;
    @NotNull
    private final ActionableEventMatcher actionableEventMatcher;

    EvidenceDatabase(@NotNull final KnownEventResolver knownEventResolver,
            @NotNull final ActionableEventMatcher actionableEventMatcher) {
        this.knownEventResolver = knownEventResolver;
        this.actionableEventMatcher = actionableEventMatcher;
    }

    @Nullable
    public GeneAlteration lookupGeneAlteration(@NotNull PurpleVariant variant) {
        return knownEventResolver.resolveForVariant(variant);
    }

    @NotNull
    public ActionabilityMatch matchToActionableEvidence(@NotNull PurpleVariant variant) {
        return actionableEventMatcher.matchForVariant(variant);
    }

    @Nullable
    public GeneAlteration lookupGeneAlteration(@NotNull PurpleCopyNumber copyNumber) {
        return knownEventResolver.resolveForCopyNumber(copyNumber);
    }

    @NotNull
    public ActionabilityMatch matchToActionableEvidence(@NotNull PurpleCopyNumber copyNumber) {
        return actionableEventMatcher.matchForCopyNumber(copyNumber);
    }

    @Nullable
    public GeneAlteration lookupGeneAlteration(@NotNull LinxHomozygousDisruption homozygousDisruption) {
        return knownEventResolver.resolveForHomozygousDisruption(homozygousDisruption);
    }

    @NotNull
    public ActionabilityMatch matchToActionableEvidence(@NotNull LinxHomozygousDisruption homozygousDisruption) {
        return actionableEventMatcher.matchForHomozygousDisruption(homozygousDisruption);
    }

    @Nullable
    public GeneAlteration lookupGeneAlteration(@NotNull LinxDisruption disruption) {
        return knownEventResolver.resolveForDisruption(disruption);
    }

    @NotNull
    public ActionabilityMatch matchToActionableEvidence(@NotNull LinxDisruption disruption) {
        return actionableEventMatcher.matchForDisruption(disruption);
    }

    @Nullable
    public KnownFusion lookupKnownFusion(@NotNull LinxFusion fusion) {
        return knownEventResolver.resolveForFusion(fusion);
    }

    @NotNull
    public ActionabilityMatch matchToActionableEvidence(@NotNull LinxFusion fusion) {
        return actionableEventMatcher.matchForFusion(fusion);
    }

    @NotNull
    public ActionabilityMatch matchToActionableEvidence(@NotNull VirusInterpreterEntry virus) {
        return actionableEventMatcher.matchForVirus(virus);
    }
}
