package com.hartwig.actin.algo.evaluation.priortumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasHistoryOfSecondMalignancy implements EvaluationFunction {

    HasHistoryOfSecondMalignancy() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        EvaluationResult result = !record.clinical().priorSecondPrimaries().isEmpty() ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has no previous second malignancy");
            builder.addFailGeneralMessages("No previous second malignancy");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has second malignancy");
            builder.addPassGeneralMessages("Presence of second malignancy");
        }

        return builder.build();
    }
}
