package com.hartwig.actin.molecular.datamodel.evidence;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

public final class TestExternalTrialFactory {

    @NotNull
    public static ImmutableExternalTrial.Builder builder() {
        return ImmutableExternalTrial.builder().title("").url("").nctId("");
    }

    @NotNull
    public static ExternalTrial createTestTrial() {
        return builder().title("treatment")
                .countries(Set.of(Country.NETHERLANDS, Country.BELGIUM))
                .url("https://clinicaltrials.gov/study/NCT00000001")
                .nctId("NCT00000001")
                .build();
    }

}
