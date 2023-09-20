package com.hartwig.actin.molecular.orange.evidence;

import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityMatch;
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionableEventMatcher;
import com.hartwig.actin.molecular.orange.evidence.known.KnownEventResolver;
import com.hartwig.hmftools.datamodel.virus.AnnotatedVirus;
import com.hartwig.serve.datamodel.common.GeneAlteration;
import com.hartwig.serve.datamodel.fusion.KnownFusion;
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss;
import com.hartwig.hmftools.datamodel.purple.PurpleVariant;
import com.hartwig.hmftools.datamodel.linx.LinxBreakend;
import com.hartwig.hmftools.datamodel.linx.LinxFusion;
import com.hartwig.hmftools.datamodel.linx.HomozygousDisruption;

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
    public GeneAlteration geneAlterationForCopyNumber(@NotNull PurpleGainLoss gainLoss) {
        return knownEventResolver.resolveForCopyNumber(gainLoss);
    }

    @NotNull
    public ActionabilityMatch evidenceForCopyNumber(@NotNull PurpleGainLoss gainLoss) {
        return actionableEventMatcher.matchForCopyNumber(gainLoss);
    }

    @Nullable
    public GeneAlteration geneAlterationForHomozygousDisruption(@NotNull HomozygousDisruption homozygousDisruption) {
        return knownEventResolver.resolveForHomozygousDisruption(homozygousDisruption);
    }

    @NotNull
    public ActionabilityMatch evidenceForHomozygousDisruption(@NotNull HomozygousDisruption homozygousDisruption) {
        return actionableEventMatcher.matchForHomozygousDisruption(homozygousDisruption);
    }

    @Nullable
    public GeneAlteration geneAlterationForBreakend(@NotNull LinxBreakend breakend) {
        return knownEventResolver.resolveForBreakend(breakend);
    }

    @NotNull
    public ActionabilityMatch evidenceForBreakend(@NotNull LinxBreakend breakend) {
        return actionableEventMatcher.matchForBreakend(breakend);
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
    public ActionabilityMatch evidenceForVirus(@NotNull AnnotatedVirus virus) {
        return actionableEventMatcher.matchForVirus(virus);
    }
}
