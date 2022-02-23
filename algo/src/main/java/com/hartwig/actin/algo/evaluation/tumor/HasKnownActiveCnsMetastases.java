package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasKnownActiveCnsMetastases implements EvaluationFunction {

    HasKnownActiveCnsMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasKnownActiveCnsMetastases = record.clinical().tumor().hasActiveCnsLesions();
        if (hasKnownActiveCnsMetastases == null) {
            return ImmutableEvaluation.builder().result(EvaluationResult.FAIL)
                    .addFailMessages("Data regarding presence of active CNS metastases is missing")
                    .build();
        }

        EvaluationResult result = hasKnownActiveCnsMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("No known active CNS metastases present");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassMessages("Active CNS metastases are present");
        }

        return builder.build();
    }
}
