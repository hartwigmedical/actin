package com.hartwig.actin.algo.evaluation;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EligibilityEvaluation;

import org.jetbrains.annotations.NotNull;

public interface EvaluationFunction {

    @NotNull
    EligibilityEvaluation evaluate(@NotNull PatientRecord record);
}
