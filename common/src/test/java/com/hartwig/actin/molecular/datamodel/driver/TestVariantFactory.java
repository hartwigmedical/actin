package com.hartwig.actin.molecular.datamodel.driver;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestVariantFactory {

    private TestVariantFactory() {
    }

    @NotNull
    public static ImmutableVariant.Builder builder() {
        return ImmutableVariant.builder()
                .event(Strings.EMPTY)
                .driverLikelihood(DriverLikelihood.LOW)
                .gene(Strings.EMPTY)
                .impact(Strings.EMPTY)
                .variantCopyNumber(0D)
                .totalCopyNumber(0D)
                .driverType(VariantDriverType.VUS)
                .clonalLikelihood(0D);
    }
}
