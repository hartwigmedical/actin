package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasKnownSymptomaticBrainMetastases implements EvaluationFunction {

    HasKnownSymptomaticBrainMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasKnownSymptomaticBrainMetastases = record.clinical().tumor().hasSymptomaticBrainLesions();
        if (hasKnownSymptomaticBrainMetastases == null) {
            return ImmutableEvaluation.builder().result(EvaluationResult.FAIL)
                    .addFailMessages("Data regarding presence of symptomatic brain metastases is missing")
                    .build();
        }

        EvaluationResult result = hasKnownSymptomaticBrainMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("No known symptomatic brain metastases present");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassMessages("Symptomatic brain metastases are present");
        }

        return builder.build();
    }
}
