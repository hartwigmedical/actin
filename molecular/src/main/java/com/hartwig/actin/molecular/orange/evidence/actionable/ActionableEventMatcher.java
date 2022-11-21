package com.hartwig.actin.molecular.orange.evidence.actionable;

import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.serve.datamodel.ActionableEvents;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActionableEventMatcher {

    @NotNull
    private final ActionableEvents actionableEvents;
    @NotNull
    private final PersonalizedActionabilityFactory personalizedActionabilityFactory;

    public ActionableEventMatcher(@NotNull final ActionableEvents actionableEvents,
            @NotNull final PersonalizedActionabilityFactory personalizedActionabilityFactory) {
        this.actionableEvents = actionableEvents;
        this.personalizedActionabilityFactory = personalizedActionabilityFactory;
    }

    @NotNull
    public ActionabilityMatch matchForMicrosatelliteStatus(@Nullable Boolean isMicrosatelliteUnstable) {
        return personalizedActionabilityFactory.create(CharacteristicsEvidence.findMicrosatelliteMatches(actionableEvents,
                isMicrosatelliteUnstable));
    }

    @NotNull
    public ActionabilityMatch matchForHomologousRepairStatus(@Nullable Boolean isHomologousRepairDeficient) {
        return personalizedActionabilityFactory.create(CharacteristicsEvidence.findHomologousRepairMatches(actionableEvents,
                isHomologousRepairDeficient));
    }

    @NotNull
    public ActionabilityMatch matchForHighTumorMutationalBurden(@Nullable Boolean hasHighTumorMutationalBurden) {
        return personalizedActionabilityFactory.create(CharacteristicsEvidence.findTumorBurdenMatches(actionableEvents,
                hasHighTumorMutationalBurden));
    }

    @NotNull
    public ActionabilityMatch matchForHighTumorMutationalLoad(@Nullable Boolean hasHighTumorMutationalLoad) {
        return personalizedActionabilityFactory.create(CharacteristicsEvidence.findTumorLoadMatches(actionableEvents,
                hasHighTumorMutationalLoad));
    }

    @NotNull
    public ActionabilityMatch matchForVariant(@NotNull PurpleVariant variant) {
        return personalizedActionabilityFactory.create(VariantEvidence.findMatches(actionableEvents, variant));
    }

    @NotNull
    public ActionabilityMatch matchForCopyNumber(@NotNull PurpleCopyNumber copyNumber) {
        return personalizedActionabilityFactory.create(CopyNumberEvidence.findMatches(actionableEvents, copyNumber));
    }

    @NotNull
    public ActionabilityMatch matchForHomozygousDisruption(@NotNull LinxHomozygousDisruption homozygousDisruption) {
        return personalizedActionabilityFactory.create(HomozygousDisruptionEvidence.findMatches(actionableEvents, homozygousDisruption));
    }

    @NotNull
    public ActionabilityMatch matchForDisruption(@NotNull LinxDisruption disruption) {
        return personalizedActionabilityFactory.create(DisruptionEvidence.findMatches(actionableEvents, disruption));
    }

    @NotNull
    public ActionabilityMatch matchForFusion(@NotNull LinxFusion fusion) {
        return personalizedActionabilityFactory.create(FusionEvidence.findMatches(actionableEvents, fusion));
    }

    @NotNull
    public ActionabilityMatch matchForVirus(@NotNull VirusInterpreterEntry virus) {
        return personalizedActionabilityFactory.create(VirusEvidence.findMatches(actionableEvents, virus));
    }

}
