package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasParticipatedInCurrentTrial implements EvaluationFunction {

    HasParticipatedInCurrentTrial() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return ImmutableEvaluation.builder().result(EvaluationResult.NOT_EVALUATED)
                .addPassMessages("Currently it is assumed that patient has not participated to current trial before")
                .build();
    }
}
