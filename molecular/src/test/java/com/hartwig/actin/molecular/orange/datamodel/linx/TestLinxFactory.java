package com.hartwig.actin.molecular.orange.datamodel.linx;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestLinxFactory {

    private TestLinxFactory() {
    }

    @NotNull
    public static ImmutableLinxStructuralVariant.Builder structuralVariantBuilder() {
        return ImmutableLinxStructuralVariant.builder().svId(0).clusterId(0);
    }

    @NotNull
    public static ImmutableLinxHomozygousDisruption.Builder homozygousDisruptionBuilder() {
        return ImmutableLinxHomozygousDisruption.builder().gene(Strings.EMPTY);
    }

    @NotNull
    public static ImmutableLinxBreakend.Builder breakendBuilder() {
        return ImmutableLinxBreakend.builder()
                .reported(true)
                .svId(0)
                .gene(Strings.EMPTY)
                .type(LinxBreakendType.BND)
                .junctionCopyNumber(0D)
                .undisruptedCopyNumber(0D)
                .regionType(LinxRegionType.INTRONIC)
                .codingType(LinxCodingType.NON_CODING);
    }

    @NotNull
    public static ImmutableLinxFusion.Builder fusionBuilder() {
        return ImmutableLinxFusion.builder()
                .reported(true)
                .type(LinxFusionType.NONE)
                .geneStart(Strings.EMPTY)
                .geneTranscriptStart(Strings.EMPTY)
                .fusedExonUp(0)
                .geneEnd(Strings.EMPTY)
                .geneTranscriptEnd(Strings.EMPTY)
                .fusedExonDown(0)
                .driverLikelihood(LinxFusionDriverLikelihood.LOW);
    }
}
