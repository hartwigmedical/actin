package com.hartwig.actin.algo.evaluation.othercondition;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasSignificantConcomitantIllness implements EvaluationFunction {

    HasSignificantConcomitantIllness() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return !record.clinical().priorOtherConditions().isEmpty() ? Evaluation.PASS : Evaluation.FAIL;
    }
}
