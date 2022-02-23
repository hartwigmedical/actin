package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasKnownBrainMetastases implements EvaluationFunction {

    HasKnownBrainMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasKnownBrainMetastases = record.clinical().tumor().hasBrainLesions();
        if (hasKnownBrainMetastases == null) {
            return ImmutableEvaluation.builder().result(EvaluationResult.FAIL)
                    .addFailMessages("Data regarding presence of brain metastases is missing")
                    .build();
        }

        EvaluationResult result = hasKnownBrainMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("No known brain metastases present");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassMessages("Brain metastases are present");
        }

        return builder.build();
    }
}
