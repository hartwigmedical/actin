package com.hartwig.actin.molecular.orange.datamodel.purple;

import java.util.Collections;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestPurpleFactory {

    private TestPurpleFactory() {
    }

    @NotNull
    public static ImmutablePurpleFit.Builder fitBuilder() {
        return ImmutablePurpleFit.builder()
                .hasSufficientQuality(false)
                .containsTumorCells(false)
                .purity(0)
                .ploidy(0)
                .qcStatuses(Collections.emptySet());
    }

    @NotNull
    public static ImmutablePurpleCharacteristics.Builder characteristicsBuilder() {
        return ImmutablePurpleCharacteristics.builder()
                .microsatelliteStatus(PurpleMicrosatelliteStatus.UNKNOWN)
                .tumorMutationalBurdenPerMb(0D)
                .tumorMutationalBurdenStatus(PurpleTumorMutationalStatus.UNKNOWN)
                .tumorMutationalLoad(0)
                .tumorMutationalLoadStatus(PurpleTumorMutationalStatus.UNKNOWN);
    }

    @NotNull
    public static ImmutablePurpleDriver.Builder driverBuilder() {
        return ImmutablePurpleDriver.builder()
                .gene(Strings.EMPTY)
                .transcript(Strings.EMPTY)
                .type(PurpleDriverType.MUTATION)
                .driverLikelihood(0D);
    }

    @NotNull
    public static ImmutablePurpleVariant.Builder variantBuilder() {
        return ImmutablePurpleVariant.builder()
                .reported(true)
                .type(PurpleVariantType.SNP)
                .gene(Strings.EMPTY)
                .chromosome(Strings.EMPTY)
                .position(0)
                .ref(Strings.EMPTY)
                .alt(Strings.EMPTY)
                .adjustedCopyNumber(0D)
                .variantCopyNumber(0D)
                .hotspot(PurpleHotspotType.NON_HOTSPOT)
                .subclonalLikelihood(0D)
                .biallelic(false)
                .canonicalImpact(transcriptImpactBuilder().build());
    }

    @NotNull
    public static ImmutablePurpleTranscriptImpact.Builder transcriptImpactBuilder() {
        return ImmutablePurpleTranscriptImpact.builder()
                .transcript(Strings.EMPTY)
                .hgvsCodingImpact(Strings.EMPTY)
                .hgvsProteinImpact(Strings.EMPTY)
                .spliceRegion(false)
                .codingEffect(PurpleCodingEffect.UNDEFINED);
    }

    @NotNull
    public static ImmutablePurpleGainLoss.Builder gainLossBuilder() {
        return ImmutablePurpleGainLoss.builder()
                .gene(Strings.EMPTY)
                .interpretation(PurpleGainLossInterpretation.FULL_LOSS)
                .minCopies(0)
                .maxCopies(0);
    }
}
