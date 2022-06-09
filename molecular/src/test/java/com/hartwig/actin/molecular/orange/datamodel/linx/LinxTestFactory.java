package com.hartwig.actin.molecular.orange.datamodel.linx;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class LinxTestFactory {

    private LinxTestFactory() {
    }

    @NotNull
    public static ImmutableLinxDisruption.Builder disruptionBuilder() {
        return ImmutableLinxDisruption.builder()
                .gene(Strings.EMPTY)
                .type(Strings.EMPTY)
                .junctionCopyNumber(0D)
                .undisruptedCopyNumber(0D)
                .range(Strings.EMPTY);
    }
}
