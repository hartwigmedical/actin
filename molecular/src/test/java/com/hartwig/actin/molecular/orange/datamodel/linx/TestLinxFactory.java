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
                .type(Strings.EMPTY)
                .junctionCopyNumber(0D)
                .undisruptedCopyNumber(0D)
                .clusterId(0);
    }

    @NotNull
    public static ImmutableLinxFusion.Builder fusionBuilder() {
        return ImmutableLinxFusion.builder()
                .reported(true)
                .type(FusionType.NONE)
                .geneStart(Strings.EMPTY)
                .geneTranscriptStart(Strings.EMPTY)
                .fusedExonUp(0)
                .geneEnd(Strings.EMPTY)
                .geneTranscriptEnd(Strings.EMPTY)
                .fusedExonDown(0)
                .driverLikelihood(FusionDriverLikelihood.LOW);
    }
}
