package com.hartwig.actin.algo.evaluation.lifestyle;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class IsWillingToNotUseContactLenses implements EvaluationFunction {

    IsWillingToNotUseContactLenses() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return ImmutableEvaluation.builder()
                .result(EvaluationResult.WARN)
                .addWarnSpecificMessages("Currently assumed that patient is willing/able not to use contact lenses")
                .build();
    }
}

