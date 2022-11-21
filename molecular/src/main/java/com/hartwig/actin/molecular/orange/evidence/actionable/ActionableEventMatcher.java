package com.hartwig.actin.molecular.orange.evidence.actionable;

import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;

import org.jetbrains.annotations.NotNull;

public class ActionableEventMatcher {

    @NotNull
    private final PersonalizedActionabilityFactory personalizedActionabilityFactory;
    @NotNull
    private final CharacteristicsEvidence characteristicsEvidence;
    @NotNull
    private final VariantEvidence variantEvidence;
    @NotNull
    private final CopyNumberEvidence copyNumberEvidence;
    @NotNull
    private final HomozygousDisruptionEvidence homozygousDisruptionEvidence;
    @NotNull
    private final DisruptionEvidence disruptionEvidence;
    @NotNull
    private final FusionEvidence fusionEvidence;
    @NotNull
    private final VirusEvidence virusEvidence;

    public ActionableEventMatcher(@NotNull final PersonalizedActionabilityFactory personalizedActionabilityFactory,
            @NotNull final CharacteristicsEvidence characteristicsEvidence, @NotNull final VariantEvidence variantEvidence,
            @NotNull final CopyNumberEvidence copyNumberEvidence, @NotNull final HomozygousDisruptionEvidence homozygousDisruptionEvidence,
            @NotNull final DisruptionEvidence disruptionEvidence, @NotNull final FusionEvidence fusionEvidence,
            @NotNull final VirusEvidence virusEvidence) {
        this.personalizedActionabilityFactory = personalizedActionabilityFactory;
        this.characteristicsEvidence = characteristicsEvidence;
        this.variantEvidence = variantEvidence;
        this.copyNumberEvidence = copyNumberEvidence;
        this.homozygousDisruptionEvidence = homozygousDisruptionEvidence;
        this.disruptionEvidence = disruptionEvidence;
        this.fusionEvidence = fusionEvidence;
        this.virusEvidence = virusEvidence;
    }

    @NotNull
    public ActionabilityMatch matchForMicrosatelliteStatus(boolean isMicrosatelliteUnstable) {
        return personalizedActionabilityFactory.create(characteristicsEvidence.findMicrosatelliteMatches(isMicrosatelliteUnstable));
    }

    @NotNull
    public ActionabilityMatch matchForHomologousRepairStatus(boolean isHomologousRepairDeficient) {
        return personalizedActionabilityFactory.create(characteristicsEvidence.findHomologousRepairMatches(isHomologousRepairDeficient));
    }

    @NotNull
    public ActionabilityMatch matchForHighTumorMutationalBurden(boolean hasHighTumorMutationalBurden) {
        return personalizedActionabilityFactory.create(characteristicsEvidence.findTumorBurdenMatches(hasHighTumorMutationalBurden));
    }

    @NotNull
    public ActionabilityMatch matchForHighTumorMutationalLoad(boolean hasHighTumorMutationalLoad) {
        return personalizedActionabilityFactory.create(characteristicsEvidence.findTumorLoadMatches(hasHighTumorMutationalLoad));
    }

    @NotNull
    public ActionabilityMatch matchForVariant(@NotNull PurpleVariant variant) {
        return personalizedActionabilityFactory.create(variantEvidence.findMatches(variant));
    }

    @NotNull
    public ActionabilityMatch matchForCopyNumber(@NotNull PurpleCopyNumber copyNumber) {
        return personalizedActionabilityFactory.create(copyNumberEvidence.findMatches(copyNumber));
    }

    @NotNull
    public ActionabilityMatch matchForHomozygousDisruption(@NotNull LinxHomozygousDisruption homozygousDisruption) {
        return personalizedActionabilityFactory.create(homozygousDisruptionEvidence.findMatches(homozygousDisruption));
    }

    @NotNull
    public ActionabilityMatch matchForDisruption(@NotNull LinxDisruption disruption) {
        return personalizedActionabilityFactory.create(disruptionEvidence.findMatches(disruption));
    }

    @NotNull
    public ActionabilityMatch matchForFusion(@NotNull LinxFusion fusion) {
        return personalizedActionabilityFactory.create(fusionEvidence.findMatches(fusion));
    }

    @NotNull
    public ActionabilityMatch matchForVirus(@NotNull VirusInterpreterEntry virus) {
        return personalizedActionabilityFactory.create(virusEvidence.findMatches(virus));
    }

}
