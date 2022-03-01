package com.hartwig.actin.algo.evaluation.othercondition;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.EvaluationFactory;

import org.jetbrains.annotations.NotNull;

public class HasAdequateVeinAccessCreator implements EvaluationFunction {

    HasAdequateVeinAccessCreator() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        // Currently cannot be determined
        return ImmutableEvaluation.builder()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedMessages("Currently adequate vein access cannot be determined")
                .build();
    }
}
