package com.hartwig.actin.algo.util;

import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.datamodel.TrialIdentification;

import org.jetbrains.annotations.NotNull;

public final class EligibilityDisplay {

    private EligibilityDisplay() {
    }

    @NotNull
    public static String trialName(@NotNull TrialIdentification trial) {
        return trial.trialId() + " (" + trial.acronym() + ")";
    }

    @NotNull
    public static String cohortName(@NotNull TrialIdentification trial, @NotNull CohortMetadata cohort) {
        return trial.trialId() + " - " + cohort.description();
    }
}
