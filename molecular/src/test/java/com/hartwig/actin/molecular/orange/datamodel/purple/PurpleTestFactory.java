package com.hartwig.actin.molecular.orange.datamodel.purple;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class PurpleTestFactory {

    private PurpleTestFactory() {
    }

    @NotNull
    public static ImmutablePurpleGainLoss.Builder gainLossBuilder() {
        return ImmutablePurpleGainLoss.builder().gene(Strings.EMPTY).interpretation(GainLossInterpretation.FULL_LOSS).minCopies(0);
    }
}
