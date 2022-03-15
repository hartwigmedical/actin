package com.hartwig.actin.algo.evaluation.toxicity;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasHistoryOfAnaphylaxis implements EvaluationFunction {

    HasHistoryOfAnaphylaxis() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        EvaluationResult result = record.clinical().allergies().isEmpty() ? EvaluationResult.FAIL : EvaluationResult.UNDETERMINED;

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has no known history of anaphylaxis");
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedSpecificMessages("Cannot be determined if patient has history of anaphylaxis");
        }

        return builder.build();
    }
}
