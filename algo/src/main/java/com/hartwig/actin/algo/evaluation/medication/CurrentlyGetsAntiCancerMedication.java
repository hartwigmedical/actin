package com.hartwig.actin.algo.evaluation.medication;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsAntiCancerMedication implements EvaluationFunction {

    CurrentlyGetsAntiCancerMedication() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return Evaluation.NOT_EVALUATED;
    }
}