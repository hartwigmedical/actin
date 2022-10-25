package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasKnownActiveCnsMetastases implements EvaluationFunction {

    HasKnownActiveCnsMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasCnsMetastases = record.clinical().tumor().hasCnsLesions();
        Boolean hasActiveCnsLesions = record.clinical().tumor().hasActiveCnsLesions();
        Boolean hasBrainMetastases = record.clinical().tumor().hasBrainLesions();
        Boolean hasActiveBrainMetastases = record.clinical().tumor().hasActiveBrainLesions();

        // If a patient is known to have no cns metastases, update active to false in case it is unknown.
        if (hasCnsMetastases != null && !hasCnsMetastases) {
            hasActiveCnsLesions = hasActiveCnsLesions != null ? hasActiveCnsLesions : false;
        }

        // If a patient is known to have no brain metastases, update active to false in case it is unknown.
        if (hasBrainMetastases != null && !hasBrainMetastases) {
            hasActiveBrainMetastases = hasActiveBrainMetastases != null ? hasActiveBrainMetastases : false;
        }

        if (hasActiveCnsLesions == null && hasActiveBrainMetastases == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Data regarding presence of active CNS metastases is missing")
                    .addUndeterminedGeneralMessages("Missing active CNS metastases data")
                    .build();
        }

        boolean hasActiveCnsMetastases = hasActiveCnsLesions != null && hasActiveCnsLesions;

        boolean hasAtLeastActiveBrainMetastases = false;
        if (hasActiveBrainMetastases != null && hasActiveBrainMetastases) {
            hasActiveCnsMetastases = true;
            hasAtLeastActiveBrainMetastases = true;
        }

        EvaluationResult result = hasActiveCnsMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;

        //TODO: Check code
        if (result == EvaluationResult.FAIL) {
            ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
            builder.addFailSpecificMessages("No known active CNS metastases present");
            builder.addFailGeneralMessages("No known active CNS metastases");
            return builder.build();
        } else if (result == EvaluationResult.PASS) {
            ImmutableEvaluation.Builder builder = EvaluationFactory.recoverable().result(result);
            if (hasAtLeastActiveBrainMetastases) {
                builder.addPassSpecificMessages("Active brain metastases are present");
                builder.addPassGeneralMessages("Active brain metastases");
            } else {
                builder.addPassSpecificMessages("Active CNS metastases are present");
                builder.addPassGeneralMessages("Active CNS metastases");
            }
            return builder.build();
        }
        return null;
    }
}
