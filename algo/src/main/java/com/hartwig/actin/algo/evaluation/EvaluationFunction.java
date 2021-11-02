package com.hartwig.actin.algo.evaluation;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;

import org.jetbrains.annotations.NotNull;

public interface EvaluationFunction {

    @NotNull
    Evaluation evaluate(@NotNull PatientRecord record);
}
