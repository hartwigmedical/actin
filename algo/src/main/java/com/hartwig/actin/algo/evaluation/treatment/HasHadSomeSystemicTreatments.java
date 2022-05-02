package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasHadSomeSystemicTreatments implements EvaluationFunction {

    private final int minSystemicTreatments;

    HasHadSomeSystemicTreatments(final int minSystemicTreatments) {
        this.minSystemicTreatments = minSystemicTreatments;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        int minSystemicCount = SystemicTreatmentAnalyser.minSystemicTreatments(record.clinical().priorTumorTreatments());
        int maxSystemicCount = SystemicTreatmentAnalyser.maxSystemicTreatments(record.clinical().priorTumorTreatments());

        EvaluationResult result;
        if (minSystemicCount >= minSystemicTreatments) {
            result = EvaluationResult.PASS;
        } else if (maxSystemicCount >= minSystemicTreatments) {
            result = EvaluationResult.UNDETERMINED;
        } else {
            result = EvaluationResult.FAIL;
        }

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient did not receive at least " + minSystemicTreatments + " systemic treatments");
            builder.addFailGeneralMessages("Nr of systemic treatments");
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedSpecificMessages(
                    "Could not determine if patient received at least " + minSystemicTreatments + " systemic treatments");
            builder.addUndeterminedGeneralMessages("Nr of systemic treatments");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient received at least " + minSystemicTreatments + " systemic treatments");
            builder.addPassGeneralMessages("Nr of systemic treatments");
        }

        return builder.build();
    }
}
