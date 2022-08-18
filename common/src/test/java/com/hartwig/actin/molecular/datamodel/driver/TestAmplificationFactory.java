package com.hartwig.actin.molecular.datamodel.driver;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestAmplificationFactory {

    private TestAmplificationFactory() {
    }

    @NotNull
    public static ImmutableAmplification.Builder builder() {
        return ImmutableAmplification.builder()
                .event(Strings.EMPTY)
                .driverLikelihood(DriverLikelihood.LOW)
                .gene(Strings.EMPTY)
                .isPartial(false)
                .copies(0);
    }
}
