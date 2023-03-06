package com.hartwig.actin.report.interpretation;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class EvaluatedCohortTestFactory {

    private EvaluatedCohortTestFactory() {
    }

    @NotNull
    public static ImmutableEvaluatedCohort.Builder builder() {
        return ImmutableEvaluatedCohort.builder()
                .trialId(Strings.EMPTY)
                .acronym(Strings.EMPTY)
                .isPotentiallyEligible(false)
                .isOpen(false)
                .hasSlotsAvailable(false);
    }
}
