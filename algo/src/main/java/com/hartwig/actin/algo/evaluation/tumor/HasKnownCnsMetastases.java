package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasKnownCnsMetastases implements EvaluationFunction {

    HasKnownCnsMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasKnownCnsMetastases = record.clinical().tumor().hasCnsLesions();

        if (hasKnownCnsMetastases == null) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Data regarding presence of CNS metastases is missing, assuming there are none")
                    .build();
        }

        EvaluationResult result = hasKnownCnsMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("No known CNS metastases present");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("CNS metastases are present");
        }

        return builder.build();
    }
}
