package com.hartwig.actin.report.interpretation

import org.apache.logging.log4j.util.Strings

object EvaluatedCohortTestFactory {
    fun builder(): ImmutableEvaluatedCohort.Builder {
        return ImmutableEvaluatedCohort.builder()
            .trialId(Strings.EMPTY)
            .acronym(Strings.EMPTY)
            .isPotentiallyEligible(false)
            .isOpen(false)
            .hasSlotsAvailable(false)
    }
}