package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasKnownActiveBrainMetastases implements EvaluationFunction {

    HasKnownActiveBrainMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasKnownActiveBrainMetastases = record.clinical().tumor().hasActiveBrainLesions();

        if (hasKnownActiveBrainMetastases == null) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Data regarding presence of active brain metastases is missing")
                    .build();
        }

        EvaluationResult result = hasKnownActiveBrainMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("No known active brain metastases present");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Active brain metastases are present");
        }

        return builder.build();
    }
}
