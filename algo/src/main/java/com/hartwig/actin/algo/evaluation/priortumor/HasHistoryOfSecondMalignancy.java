package com.hartwig.actin.algo.evaluation.priortumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;

import org.jetbrains.annotations.NotNull;

//TODO: Review implementation and test
public class HasHistoryOfSecondMalignancy implements EvaluationFunction {

    HasHistoryOfSecondMalignancy() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasMatch = false;
        for (PriorSecondPrimary priorSecondPrimary : record.clinical().priorSecondPrimaries()) {
            if (priorSecondPrimary != null) {
                hasMatch = true;
            }
        }

        EvaluationResult result = hasMatch ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has no previous second malignancy");
            builder.addFailGeneralMessages("No previous malignancy");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has previous second malignancy");
            builder.addPassGeneralMessages("Previous primary tumor history");
        }

        return builder.build();
    }
}
