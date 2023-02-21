package com.hartwig.actin.clinical.datamodel;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestPriorOtherConditionFactory {

    private TestPriorOtherConditionFactory() {
    }

    @NotNull
    public static ImmutablePriorOtherCondition.Builder builder() {
        return ImmutablePriorOtherCondition.builder().name(Strings.EMPTY).category(Strings.EMPTY).isContraindicationForTherapy(false);
    }
}
