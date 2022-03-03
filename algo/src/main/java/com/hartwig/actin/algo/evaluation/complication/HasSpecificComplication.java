package com.hartwig.actin.algo.evaluation.complication;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.Complication;

import org.jetbrains.annotations.NotNull;

public class HasSpecificComplication implements EvaluationFunction {

    @NotNull
    private final String termToFind;

    HasSpecificComplication(@NotNull final String termToFind) {
        this.termToFind = termToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (Complication complication : record.clinical().complications()) {
            if (complication.name().contains(termToFind)) {
                return ImmutableEvaluation.builder()
                        .result(EvaluationResult.PASS)
                        .addPassMessages("Patient has complication " + termToFind)
                        .build();

            }
        }

        return ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addFailMessages("Patient does not have complication " + termToFind)
                .build();
    }
}
