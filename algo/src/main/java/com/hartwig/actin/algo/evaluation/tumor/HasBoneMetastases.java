package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasBoneMetastases implements EvaluationFunction {

    HasBoneMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasBoneMetastases = record.clinical().tumor().hasBoneLesions();
        if (hasBoneMetastases == null) {
            return ImmutableEvaluation.builder().result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedMessages("Data regarding presence of bone metastases is missing")
                    .build();

        }

        EvaluationResult result = hasBoneMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("No bone metastases present");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassMessages("Bone metastases are present");
        }

        return builder.build();
    }
}
