package com.hartwig.actin.algo.evaluation.vitalfunction;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasRestingHeartRateWithinBounds implements EvaluationFunction {

    //TODO: implement according to README
    HasRestingHeartRateWithinBounds() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return ImmutableEvaluation.builder()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Resting heart rate not captured by ACTIN")
                .build();
    }
}
