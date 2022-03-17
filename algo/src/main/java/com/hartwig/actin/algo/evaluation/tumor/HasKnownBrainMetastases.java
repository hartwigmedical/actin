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
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Data regarding presence of brain metastases is missing, assuming there are none")
                    .addFailGeneralMessages("Assuming no known brain metastases")
                    .build();
        }

        EvaluationResult result = hasKnownBrainMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("No known brain metastases present");
            builder.addFailGeneralMessages("No known brain metastases");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Brain metastases are present");
            builder.addPassGeneralMessages("Brain metastases");
        }

        return builder.build();
    }
}
