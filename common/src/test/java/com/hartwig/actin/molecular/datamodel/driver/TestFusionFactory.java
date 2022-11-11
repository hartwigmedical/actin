package com.hartwig.actin.molecular.datamodel.driver;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestFusionFactory {

    private TestFusionFactory() {
    }

    @NotNull
    public static ImmutableFusion.Builder builder() {
        return ImmutableFusion.builder()
                .from(TestDriverFactory.createEmptyDriver())
                .geneStart(Strings.EMPTY)
                .geneTranscriptStart(Strings.EMPTY)
                .geneContextStart(Strings.EMPTY)
                .fusedExonUp(-1)
                .geneEnd(Strings.EMPTY)
                .geneTranscriptEnd(Strings.EMPTY)
                .geneContextEnd(Strings.EMPTY)
                .fusedExonDown(-1)
                .proteinEffect(ProteinEffect.UNKNOWN)
                .driverType(FusionDriverType.KNOWN_PAIR);
    }
}
