package com.hartwig.actin.molecular.datamodel.driver;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestLossFactory {

    private TestLossFactory() {
    }

    @NotNull
    public static ImmutableLoss.Builder builder() {
        return ImmutableLoss.builder()
                .event(Strings.EMPTY)
                .driverLikelihood(DriverLikelihood.LOW)
                .gene(Strings.EMPTY)
                .isPartial(false);
    }
}
