package com.hartwig.actin.algo.evaluation.othercondition;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class CanSwallowOralMedication implements EvaluationFunction {

    CanSwallowOralMedication() {
    }

    @NotNull
    @Override
    // To do: extend evaluation
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.create(EvaluationResult.UNDETERMINED);
    }
}
