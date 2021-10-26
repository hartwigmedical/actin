package com.hartwig.actin.algo.eligibility;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EligibilityEvaluation;

import org.jetbrains.annotations.NotNull;

public interface EvaluatorFunction {

    @NotNull
    EligibilityEvaluation evaluate(@NotNull PatientRecord record);
}
