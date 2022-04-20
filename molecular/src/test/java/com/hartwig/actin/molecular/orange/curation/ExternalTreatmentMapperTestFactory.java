package com.hartwig.actin.molecular.orange.curation;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.NotNull;

public final class ExternalTreatmentMapperTestFactory {

    private ExternalTreatmentMapperTestFactory() {
    }

    @NotNull
    public static ExternalTreatmentMapper create() {
        return new ExternalTreatmentMapper(Lists.newArrayList());
    }

    @NotNull
    public static ExternalTreatmentMapper create(@NotNull String externalTreatment, @NotNull String actinTreatment) {
        ExternalTreatmentMapping mapping =
                ImmutableExternalTreatmentMapping.builder().externalTreatment(externalTreatment).actinTreatment(actinTreatment).build();
        return new ExternalTreatmentMapper(Lists.newArrayList(mapping));
    }
}
