package com.hartwig.actin.algo.evaluation;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.jetbrains.annotations.NotNull;

public interface EvaluationFunction {

    @NotNull
    EvaluationResult evaluate(@NotNull PatientRecord record);
}
