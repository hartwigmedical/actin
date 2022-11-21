package com.hartwig.actin.molecular.orange.evidence;

import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityMatch;
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionableEventMatcher;
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
    public ActionabilityMatch evidenceForMicrosatelliteStatus(@Nullable Boolean isMicrosatelliteUnstable) {
        if (isMicrosatelliteUnstable == null) {
            return null;
        }

        return actionableEventMatcher.matchForMicrosatelliteStatus(isMicrosatelliteUnstable);
    }

    @Nullable
    public ActionabilityMatch evidenceForHomologousRepairStatus(@Nullable Boolean isHomologousRepairDeficient) {
        if (isHomologousRepairDeficient == null) {
            return null;
        }

        return actionableEventMatcher.matchForHomologousRepairStatus(isHomologousRepairDeficient);
    }

    @Nullable
    public ActionabilityMatch evidenceForTumorMutationalBurdenStatus(@Nullable Boolean hasHighTumorMutationalBurden) {
        if (hasHighTumorMutationalBurden == null) {
            return null;
        }

        return actionableEventMatcher.matchForHighTumorMutationalBurden(hasHighTumorMutationalBurden);
    }

    @Nullable
    public ActionabilityMatch evidenceForTumorMutationalLoadStatus(@Nullable Boolean hasHighTumorMutationalLoad) {
        if (hasHighTumorMutationalLoad == null) {
            return null;
        }

        return actionableEventMatcher.matchForHighTumorMutationalLoad(hasHighTumorMutationalLoad);
    }

    @Nullable
    public GeneAlteration geneAlterationForVariant(@NotNull PurpleVariant variant) {
        return knownEventResolver.resolveForVariant(variant);
    }

    @NotNull
    public ActionabilityMatch evidenceForVariant(@NotNull PurpleVariant variant) {
        return actionableEventMatcher.matchForVariant(variant);
    }

    @Nullable
    public GeneAlteration geneAlterationForCopyNumber(@NotNull PurpleCopyNumber copyNumber) {
        return knownEventResolver.resolveForCopyNumber(copyNumber);
    }

    @NotNull
    public ActionabilityMatch evidenceForCopyNumber(@NotNull PurpleCopyNumber copyNumber) {
        return actionableEventMatcher.matchForCopyNumber(copyNumber);
    }

    @Nullable
    public GeneAlteration geneAlterationForHomozygousDisruption(@NotNull LinxHomozygousDisruption homozygousDisruption) {
        return knownEventResolver.resolveForHomozygousDisruption(homozygousDisruption);
    }

    @NotNull
    public ActionabilityMatch evidenceForHomozygousDisruption(@NotNull LinxHomozygousDisruption homozygousDisruption) {
        return actionableEventMatcher.matchForHomozygousDisruption(homozygousDisruption);
    }

    @Nullable
    public GeneAlteration geneAlterationForDisruption(@NotNull LinxDisruption disruption) {
        return knownEventResolver.resolveForDisruption(disruption);
    }

    @NotNull
    public ActionabilityMatch evidenceForDisruption(@NotNull LinxDisruption disruption) {
        return actionableEventMatcher.matchForDisruption(disruption);
    }

    @Nullable
    public KnownFusion lookupKnownFusion(@NotNull LinxFusion fusion) {
        return knownEventResolver.resolveForFusion(fusion);
    }

    @NotNull
    public ActionabilityMatch evidenceForFusion(@NotNull LinxFusion fusion) {
        return actionableEventMatcher.matchForFusion(fusion);
    }

    @NotNull
    public ActionabilityMatch evidenceForVirus(@NotNull VirusInterpreterEntry virus) {
        return actionableEventMatcher.matchForVirus(virus);
    }
}
