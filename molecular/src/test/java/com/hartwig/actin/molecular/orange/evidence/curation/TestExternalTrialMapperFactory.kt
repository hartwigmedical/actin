package com.hartwig.actin.molecular.orange.evidence.curation;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.NotNull;

public final class TestExternalTrialMapperFactory {

    private TestExternalTrialMapperFactory() {
    }

    @NotNull
    public static ExternalTrialMapper createMinimalTestMapper() {
        return new ExternalTrialMapper(Lists.newArrayList());
    }

    @NotNull
    public static ExternalTrialMapper create(@NotNull String externalTrial, @NotNull String actinTrial) {
        ExternalTrialMapping mapping = ImmutableExternalTrialMapping.builder().externalTrial(externalTrial).actinTrial(actinTrial).build();
        return new ExternalTrialMapper(Lists.newArrayList(mapping));
    }
}
