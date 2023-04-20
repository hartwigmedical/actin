package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasKnownCnsMetastases implements EvaluationFunction {

    HasKnownCnsMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasCnsLesions = record.clinical().tumor().hasCnsLesions();
        Boolean hasBrainLesions = record.clinical().tumor().hasBrainLesions();

        if (hasCnsLesions == null && hasBrainLesions == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Data regarding presence of CNS metastases is missing, assuming there are none")
                    .addFailGeneralMessages("Assuming no known CNS metastases")
                    .build();
        }

        boolean hasKnownCnsMetastases = hasCnsLesions != null && hasCnsLesions;

        boolean hasAtLeastActiveBrainMetastases = false;
        if (hasBrainLesions != null && hasBrainLesions) {
            hasKnownCnsMetastases = true;
            hasAtLeastActiveBrainMetastases = true;
        }

        EvaluationResult result = hasKnownCnsMetastases ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("No known CNS metastases present");
            builder.addFailGeneralMessages("No known CNS metastases");
        } else if (result == EvaluationResult.PASS) {
            if (hasAtLeastActiveBrainMetastases) {
                builder.addPassSpecificMessages("Brain metastases are present, these are considered CNS metastases");
                builder.addPassGeneralMessages("Brain (CNS) metastases");
            } else {
                builder.addPassSpecificMessages("CNS metastases are present");
                builder.addPassGeneralMessages("CNS metastases");
            }
        }

        return builder.build();
    }
}
