package com.hartwig.actin.treatment.datamodel;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestTreatmentFactory {

    private static final String TEST_TRIAL = "test trial";

    private TestTreatmentFactory() {
    }

    @NotNull
    public static Trial createMinimalTestTrial() {
        return ImmutableTrial.builder().trialId(TEST_TRIAL).acronym(Strings.EMPTY).title(Strings.EMPTY).build();
    }

    @NotNull
    public static Trial createProperTestTrial() {
        return ImmutableTrial.builder().from(createMinimalTestTrial()).build();
    }
}
