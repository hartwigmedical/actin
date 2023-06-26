package com.hartwig.actin.treatment.trial.config;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestTrialDefinitionConfigFactory {

    @NotNull
    public static ImmutableTrialDefinitionConfig.Builder builder() {
        return ImmutableTrialDefinitionConfig.builder().trialId(Strings.EMPTY).open(false).acronym(Strings.EMPTY).title(Strings.EMPTY);
    }
}
