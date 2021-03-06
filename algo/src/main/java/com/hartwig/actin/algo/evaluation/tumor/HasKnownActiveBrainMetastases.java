package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
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
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Data regarding presence of active brain metastases is missing")
                    .addFailGeneralMessages("Missing active brain metastases data")
                    .build();
        }

        EvaluationResult result = hasKnownActiveBrainMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("No known active brain metastases present");
            builder.addFailGeneralMessages("No active brain metastases");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Active brain metastases are present");
            builder.addPassGeneralMessages("Active brain metastases");
        }

        return builder.build();
    }
}
