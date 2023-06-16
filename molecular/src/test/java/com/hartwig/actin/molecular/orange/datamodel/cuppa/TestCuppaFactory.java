package com.hartwig.actin.molecular.orange.datamodel.cuppa;

import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableCuppaPrediction;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestCuppaFactory {

    private TestCuppaFactory() {
    }

    @NotNull
    public static ImmutableCuppaPrediction.Builder builder() {
        return ImmutableCuppaPrediction.builder().cancerType(Strings.EMPTY).likelihood(0D);
    }
}
