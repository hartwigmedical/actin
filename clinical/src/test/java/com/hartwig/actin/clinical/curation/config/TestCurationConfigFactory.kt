package com.hartwig.actin.clinical.curation.config

import org.apache.logging.log4j.util.Strings
import java.util.*

object TestCurationConfigFactory {
    fun primaryTumorConfigBuilder(): ImmutablePrimaryTumorConfig.Builder {
        return ImmutablePrimaryTumorConfig.builder()
            .input(Strings.EMPTY)
            .primaryTumorLocation(Strings.EMPTY)
            .primaryTumorSubLocation(Strings.EMPTY)
            .primaryTumorType(Strings.EMPTY)
            .primaryTumorSubType(Strings.EMPTY)
            .primaryTumorExtraDetails(Strings.EMPTY)
    }

    fun secondPrimaryConfigBuilder(): ImmutableSecondPrimaryConfig.Builder {
        return ImmutableSecondPrimaryConfig.builder().input(Strings.EMPTY).ignore(false)
    }

    fun nonOncologicalHistoryConfigBuilder(): ImmutableNonOncologicalHistoryConfig.Builder {
        return ImmutableNonOncologicalHistoryConfig.builder()
            .input(Strings.EMPTY)
            .ignore(false)
            .lvef(Optional.empty())
            .priorOtherCondition(Optional.empty())
    }

    fun intoleranceConfigBuilder(): ImmutableIntoleranceConfig.Builder {
        return ImmutableIntoleranceConfig.builder().input(Strings.EMPTY).name(Strings.EMPTY)
    }
}