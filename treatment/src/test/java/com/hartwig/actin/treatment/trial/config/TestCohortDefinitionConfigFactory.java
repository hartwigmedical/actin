package com.hartwig.actin.treatment.trial.config;

import java.util.Set;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestCohortDefinitionConfigFactory {

    @NotNull
    public static ImmutableCohortDefinitionConfig.Builder builder() {
        return ImmutableCohortDefinitionConfig.builder()
                .trialId(Strings.EMPTY)
                .cohortId(Strings.EMPTY)
                .ctcCohortIds(Set.of())
                .evaluable(true)
                .blacklist(false)
                .description(Strings.EMPTY);
    }
}
