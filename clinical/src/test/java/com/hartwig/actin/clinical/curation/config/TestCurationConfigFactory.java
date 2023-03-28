package com.hartwig.actin.clinical.curation.config;

import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestCurationConfigFactory {

    private TestCurationConfigFactory() {
    }

    @NotNull
    public static ImmutablePrimaryTumorConfig.Builder primaryTumorConfigBuilder() {
        return ImmutablePrimaryTumorConfig.builder()
                .input(Strings.EMPTY)
                .primaryTumorLocation(Strings.EMPTY)
                .primaryTumorSubLocation(Strings.EMPTY)
                .primaryTumorType(Strings.EMPTY)
                .primaryTumorSubType(Strings.EMPTY)
                .primaryTumorExtraDetails(Strings.EMPTY);
    }

    @NotNull
    public static ImmutableSecondPrimaryConfig.Builder secondPrimaryConfigBuilder() {
        return ImmutableSecondPrimaryConfig.builder().input(Strings.EMPTY).ignore(false);
    }

    @NotNull
    public static ImmutableNonOncologicalHistoryConfig.Builder nonOncologicalHistoryConfigBuilder() {
        return ImmutableNonOncologicalHistoryConfig.builder()
                .input(Strings.EMPTY)
                .ignore(false)
                .lvef(Optional.empty())
                .priorOtherCondition(Optional.empty());
    }

    @NotNull
    public static ImmutableIntoleranceConfig.Builder intoleranceConfigBuilder() {
        return ImmutableIntoleranceConfig.builder().input(Strings.EMPTY).name(Strings.EMPTY);
    }
}
