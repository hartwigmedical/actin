package com.hartwig.actin.algo.evaluation.pregnancy;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class CanUseAdequateAnticonception implements EvaluationFunction {

    CanUseAdequateAnticonception() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull final PatientRecord record) {
        return Evaluation.NOT_EVALUATED;
    }
}
