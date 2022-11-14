package com.hartwig.actin.report.interpretation;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class EvaluatedTrialTestFactory {

    private EvaluatedTrialTestFactory() {
    }

    @NotNull
    public static ImmutableEvaluatedTrial.Builder builder() {
        return ImmutableEvaluatedTrial.builder()
                .trialId(Strings.EMPTY)
                .acronym(Strings.EMPTY)
                .isPotentiallyEligible(false)
                .isOpen(false)
                .hasSlotsAvailable(false);
    }
}
