package com.hartwig.actin.molecular.orange.curation;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.NotNull;

public final class ExternalTreatmentMapperTestFactory {

    private ExternalTreatmentMapperTestFactory() {
    }

    @NotNull
    public static ExternalTrialMapper create() {
        return new ExternalTrialMapper(Lists.newArrayList());
    }

    @NotNull
    public static ExternalTrialMapper create(@NotNull String externalTrial, @NotNull String actinTrial) {
        ExternalTrialMapping mapping = ImmutableExternalTrialMapping.builder().externalTrial(externalTrial).actinTrial(actinTrial).build();
        return new ExternalTrialMapper(Lists.newArrayList(mapping));
    }
}
