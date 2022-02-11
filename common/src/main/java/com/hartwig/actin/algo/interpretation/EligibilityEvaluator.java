package com.hartwig.actin.algo.interpretation;

import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.algo.datamodel.TrialEligibility;

import org.jetbrains.annotations.NotNull;

public final class EligibilityEvaluator {

    private EligibilityEvaluator() {
    }

    public static boolean isEligibleTrial(@NotNull TrialEligibility trial) {
        if (trial.overallEvaluation().isPass()) {
            // Either a trial has no cohorts, or at least one cohort has to pass.
            if (trial.cohorts().isEmpty()) {
                return true;
            } else {
                for (CohortEligibility cohort : trial.cohorts()) {
                    if (isEligibleCohort(cohort)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isEligibleCohort(@NotNull CohortEligibility cohort) {
        return cohort.overallEvaluation().isPass() && !cohort.metadata().blacklist();
    }
}
