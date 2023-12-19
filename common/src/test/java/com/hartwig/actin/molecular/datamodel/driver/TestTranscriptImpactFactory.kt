package com.hartwig.actin.molecular.datamodel.driver;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestTranscriptImpactFactory {

    private TestTranscriptImpactFactory() {
    }

    @NotNull
    public static ImmutableTranscriptImpact.Builder builder() {
        return ImmutableTranscriptImpact.builder()
                .transcriptId(Strings.EMPTY)
                .hgvsCodingImpact(Strings.EMPTY)
                .hgvsProteinImpact(Strings.EMPTY)
                .isSpliceRegion(false);
    }
}
