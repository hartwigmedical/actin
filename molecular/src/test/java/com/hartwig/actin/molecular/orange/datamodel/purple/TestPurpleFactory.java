package com.hartwig.actin.molecular.orange.datamodel.purple;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestPurpleFactory {

    private TestPurpleFactory() {
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
                .canonicalTranscript(Strings.EMPTY)
                .canonicalCodingEffect(PurpleCodingEffect.UNDEFINED)
                .canonicalHgvsProteinImpact(Strings.EMPTY)
                .canonicalHgvsCodingImpact(Strings.EMPTY)
                .totalCopyNumber(0D)
                .alleleCopyNumber(0D)
                .hotspot(VariantHotspot.NON_HOTSPOT)
                .clonalLikelihood(0D)
                .driverLikelihood(0D)
                .biallelic(false);
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
