package com.hartwig.actin.molecular.orange.datamodel.linx;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestLinxFactory {

    private TestLinxFactory() {
    }

    @NotNull
    public static ImmutableLinxHomozygousDisruption.Builder homozygousDisruptionBuilder() {
        return ImmutableLinxHomozygousDisruption.builder().gene(Strings.EMPTY);
    }

    @NotNull
    public static ImmutableLinxDisruption.Builder disruptionBuilder() {
        return ImmutableLinxDisruption.builder()
                .reported(true)
                .gene(Strings.EMPTY)
                .type(LinxDisruptionType.BND)
                .junctionCopyNumber(0D)
                .undisruptedCopyNumber(0D)
                .regionType(LinxRegionType.INTRONIC)
                .codingType(LinxCodingType.NON_CODING)
                .clusterId(0);
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
