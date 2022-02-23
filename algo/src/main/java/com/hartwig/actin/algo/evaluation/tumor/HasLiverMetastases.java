package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasLiverMetastases implements EvaluationFunction {

    HasLiverMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasLiverMetastases = record.clinical().tumor().hasLiverLesions();
        if (hasLiverMetastases == null) {
            return ImmutableEvaluation.builder().result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedMessages("Data regarding presence of liver metastases is missing")
                    .build();
        }

        EvaluationResult result = hasLiverMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("No liver metastases present");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassMessages("Liver metastases are present");
        }

        return builder.build();
    }
}
