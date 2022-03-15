package com.hartwig.actin.algo.evaluation.priortumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;

import org.jetbrains.annotations.NotNull;

public class HasActiveSecondMalignancy implements EvaluationFunction {

    HasActiveSecondMalignancy() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasMatch = false;
        for (PriorSecondPrimary priorSecondPrimary : record.clinical().priorSecondPrimaries()) {
            if (priorSecondPrimary.isActive()) {
                hasMatch = true;
            }
        }

        EvaluationResult result = hasMatch ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has no active second malignancy");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has active second malignancy");
        }

        return builder.build();
    }
}

