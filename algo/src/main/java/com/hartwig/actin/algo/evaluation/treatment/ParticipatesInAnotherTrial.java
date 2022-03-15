package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class ParticipatesInAnotherTrial implements EvaluationFunction {

    ParticipatesInAnotherTrial() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return ImmutableEvaluation.builder().result(EvaluationResult.NOT_EVALUATED)
                .addPassSpecificMessages("Assumed that patient is not currently participating in another trial")
                .build();
    }
}
