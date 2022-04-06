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
        Boolean hasActiveCnsLesions = record.clinical().tumor().hasActiveCnsLesions();
        Boolean hasActiveBrainLesions = record.clinical().tumor().hasActiveBrainLesions();

        if (hasActiveCnsLesions == null && hasActiveBrainLesions == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Data regarding presence of active CNS metastases is missing")
                    .build();
        }

        boolean hasActiveCnsMetastases = false;
        if (hasActiveCnsLesions != null && hasActiveCnsLesions) {
            hasActiveCnsMetastases = true;
        }

        if (hasActiveBrainLesions != null && hasActiveBrainLesions) {
            hasActiveCnsMetastases = true;
        }

        EvaluationResult result = hasActiveCnsMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("No known active CNS metastases present");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Active CNS metastases are present");
        }

        return builder.build();
    }
}
