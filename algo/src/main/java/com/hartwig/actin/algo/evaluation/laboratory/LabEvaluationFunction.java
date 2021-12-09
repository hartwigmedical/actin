package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public interface LabEvaluationFunction {

    @NotNull
    Evaluation evaluate(@NotNull LabValue labValue);
}
