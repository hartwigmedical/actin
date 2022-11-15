package com.hartwig.actin.molecular.orange.datamodel.linx;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestLinxFactory {

    private TestLinxFactory() {
    }

    @NotNull
    public static ImmutableLinxDisruption.Builder disruptionBuilder() {
        return ImmutableLinxDisruption.builder()
                .gene(Strings.EMPTY)
                .type(Strings.EMPTY)
                .junctionCopyNumber(0D)
                .undisruptedCopyNumber(0D)
                .clusterId(0);
    }

    @NotNull
    public static ImmutableLinxFusion.Builder fusionBuilder() {
        return ImmutableLinxFusion.builder()
                .type(FusionType.NONE)
                .geneStart(Strings.EMPTY)
                .geneTranscriptStart(Strings.EMPTY)
                .geneContextStart(Strings.EMPTY)
                .fusedExonUp(0)
                .geneEnd(Strings.EMPTY)
                .geneTranscriptEnd(Strings.EMPTY)
                .geneContextEnd(Strings.EMPTY)
                .fusedExonDown(0)
                .driverLikelihood(FusionDriverLikelihood.LOW);
    }
}
