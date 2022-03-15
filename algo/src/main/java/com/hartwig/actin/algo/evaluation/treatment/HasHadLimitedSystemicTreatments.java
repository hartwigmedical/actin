package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.jetbrains.annotations.NotNull;

public class HasHadLimitedSystemicTreatments implements EvaluationFunction {

    private final int maxSystemicTreatments;

    HasHadLimitedSystemicTreatments(final int maxSystemicTreatments) {
        this.maxSystemicTreatments = maxSystemicTreatments;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        int systemicCount = 0;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.isSystemic()) {
                systemicCount++;
            }
        }

        EvaluationResult result = systemicCount <= maxSystemicTreatments ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has received more than " + maxSystemicTreatments + " systemic treatments");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has received at most " + maxSystemicTreatments + " systemic treatments");
        }

        return builder.build();
    }
}
