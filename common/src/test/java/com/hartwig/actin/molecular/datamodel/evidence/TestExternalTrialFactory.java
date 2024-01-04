package com.hartwig.actin.molecular.datamodel.evidence;

import java.util.Collections;

import org.assertj.core.util.Sets;
import org.jetbrains.annotations.NotNull;

public final class TestExternalTrialFactory {

    @NotNull
    public static ImmutableExternalTrial.Builder builder() {
        return ImmutableExternalTrial.builder();
    }

    @NotNull
    public static ExternalTrial createMinimal() {
        return builder().title("treatment")
                .countries(Sets.newHashSet(Collections.singleton("country")))
                .url("https://clinicaltrials.gov/study/NCT00000001")
                .nctId("nctId")
                .build();
    }

}
