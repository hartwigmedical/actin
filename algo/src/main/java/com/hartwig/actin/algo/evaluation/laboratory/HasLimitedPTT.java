package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasLimitedPTT implements EvaluationFunction {

    HasLimitedPTT() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull final PatientRecord record) {
        return ImmutableEvaluation.builder()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedMessages("PTT cannot be determined yet")
                .build();
    }
}
