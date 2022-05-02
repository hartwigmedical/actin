package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasHadLimitedSystemicTreatments implements EvaluationFunction {

    private final int maxSystemicTreatments;

    HasHadLimitedSystemicTreatments(final int maxSystemicTreatments) {
        this.maxSystemicTreatments = maxSystemicTreatments;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        int minSystemicCount = SystemicTreatmentAnalyser.minSystemicTreatments(record.clinical().priorTumorTreatments());
        int maxSystemicCount = SystemicTreatmentAnalyser.maxSystemicTreatments(record.clinical().priorTumorTreatments());

        EvaluationResult result;
        if (maxSystemicCount <= maxSystemicTreatments) {
            result = EvaluationResult.PASS;
        } else if (minSystemicCount <= maxSystemicTreatments) {
            result = EvaluationResult.UNDETERMINED;
        } else {
            result = EvaluationResult.FAIL;
        }

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has received more than " + maxSystemicTreatments + " systemic treatments");
            builder.addFailGeneralMessages("Nr of systemic treatments");
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedSpecificMessages(
                    "Could not determine if patient received at most " + maxSystemicTreatments + " systemic treatments");
            builder.addUndeterminedGeneralMessages("Nr of systemic treatments");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has received at most " + maxSystemicTreatments + " systemic treatments");
            builder.addPassGeneralMessages("Nr of systemic treatments");
        }

        return builder.build();
    }
}
