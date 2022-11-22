package com.hartwig.actin.molecular.orange.datamodel.purple;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestPurpleFactory {

    private TestPurpleFactory() {
    }

    @NotNull
    public static ImmutablePurpleFit.Builder fitBuilder() {
        return ImmutablePurpleFit.builder()
                .hasReliableQuality(false)
                .hasReliablePurity(false)
                .purity(0)
                .ploidy(0);
    }

    @NotNull
    public static ImmutablePurpleCharacteristics.Builder characteristicsBuilder() {
        return ImmutablePurpleCharacteristics.builder()
                .microsatelliteStabilityStatus(Strings.EMPTY)
                .tumorMutationalBurden(0D)
                .tumorMutationalBurdenStatus(Strings.EMPTY)
                .tumorMutationalLoad(0)
                .tumorMutationalLoadStatus(Strings.EMPTY);
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
                .totalCopyNumber(0D)
                .alleleCopyNumber(0D)
                .hotspot(PurpleHotspotType.NON_HOTSPOT)
                .clonalLikelihood(0D)
                .driverLikelihood(0D)
                .biallelic(false)
                .canonicalImpact(transcriptImpactBuilder().build());
    }

    @NotNull
    public static ImmutablePurpleTranscriptImpact.Builder transcriptImpactBuilder() {
        return ImmutablePurpleTranscriptImpact.builder()
                .transcriptId(Strings.EMPTY)
                .hgvsCodingImpact(Strings.EMPTY)
                .hgvsProteinImpact(Strings.EMPTY)
                .spliceRegion(false)
                .codingEffect(PurpleCodingEffect.UNDEFINED);
    }

    @NotNull
    public static ImmutablePurpleCopyNumber.Builder copyNumberBuilder() {
        return ImmutablePurpleCopyNumber.builder()
                .reported(true)
                .gene(Strings.EMPTY)
                .interpretation(CopyNumberInterpretation.FULL_LOSS)
                .minCopies(0)
                .maxCopies(0);
    }
}
