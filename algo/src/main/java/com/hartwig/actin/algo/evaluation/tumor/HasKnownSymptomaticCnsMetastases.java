package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasKnownSymptomaticCnsMetastases implements EvaluationFunction {

    HasKnownSymptomaticCnsMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasKnownSymptomaticCnsMetastases = record.clinical().tumor().hasSymptomaticCnsLesions();

        if (hasKnownSymptomaticCnsMetastases == null) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.FAIL)
                    .addFailMessages("Data regarding presence of symptomatic CNS metastases is missing")
                    .build();
        }

        EvaluationResult result = hasKnownSymptomaticCnsMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("No known symptomatic CNS metastases present");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassMessages("Symptomatic CNS metastases are present");
        }

        return builder.build();
    }
}
