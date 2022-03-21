package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.jetbrains.annotations.NotNull;

public class HasHadSomeSystemicTreatments implements EvaluationFunction {

    private final int minSystemicTreatments;

    //TODO: Update according to README
    HasHadSomeSystemicTreatments(final int minSystemicTreatments) {
        this.minSystemicTreatments = minSystemicTreatments;
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

        EvaluationResult result = systemicCount >= minSystemicTreatments ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient did not receive at least " + minSystemicTreatments + " systemic treatments");
            builder.addFailGeneralMessages("Nr of systemic treatments");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient received at least " + minSystemicTreatments + " systemic treatments");
            builder.addPassGeneralMessages("Nr of systemic treatments");
        }

        return builder.build();
    }
}
