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
        Boolean hasBrainMetastases = record.clinical().tumor().hasBrainLesions();
        Boolean hasActiveBrainMetastases = record.clinical().tumor().hasActiveBrainLesions();

        // If a patient is known to have no brain metastases, update active to false in case it is unknown.
        if (hasBrainMetastases != null && !hasBrainMetastases) {
            hasActiveBrainMetastases = hasActiveBrainMetastases != null ? hasActiveBrainMetastases : false;
        }

        if (hasActiveBrainMetastases == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Data regarding presence of active brain metastases is missing")
                    .addUndeterminedGeneralMessages("Missing active brain metastases data")
                    .build();
        }

        EvaluationResult result = hasActiveBrainMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;

        //TODO: Check code
        if (result == EvaluationResult.FAIL) {
            ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
            builder.addFailSpecificMessages("No known active brain metastases present");
            builder.addFailGeneralMessages("No active brain metastases");
            return builder.build();
        } else if (result == EvaluationResult.PASS) {
            ImmutableEvaluation.Builder builder = EvaluationFactory.recoverable().result(result);
            builder.addPassSpecificMessages("Active brain metastases are present");
            builder.addPassGeneralMessages("Active brain metastases");
            return builder.build();
        }
        return null;
    }
}
