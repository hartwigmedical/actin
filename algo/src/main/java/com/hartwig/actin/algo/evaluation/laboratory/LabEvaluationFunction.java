package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public interface LabEvaluationFunction {

    @NotNull
    EvaluationResult evaluate(@NotNull PatientRecord record, @NotNull LabValue labValue);
}
