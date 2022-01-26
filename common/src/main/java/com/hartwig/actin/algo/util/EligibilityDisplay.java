package com.hartwig.actin.algo.util;

import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.algo.datamodel.TrialEligibility;

import org.jetbrains.annotations.NotNull;

public final class EligibilityDisplay {

    private EligibilityDisplay() {
    }

    @NotNull
    public static String trialName(@NotNull TrialEligibility trial) {
        return trial.identification().trialId();
    }

    @NotNull
    public static String cohortName(@NotNull TrialEligibility trial, @NotNull CohortEligibility cohort) {
        return trialName(trial) + " - " + cohort.metadata().description();
    }
}
