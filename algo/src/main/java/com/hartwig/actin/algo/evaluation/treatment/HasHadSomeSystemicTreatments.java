package com.hartwig.actin.algo.evaluation.treatment;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.jetbrains.annotations.NotNull;

public class HasHadSomeSystemicTreatments implements EvaluationFunction {

    private final int minSystemicTreatments;

    HasHadSomeSystemicTreatments(final int minSystemicTreatments) {
        this.minSystemicTreatments = minSystemicTreatments;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<PriorTumorTreatment> systemicOnly = systemicOnly(record.clinical().priorTumorTreatments());

        EvaluationResult result = systemicOnly.size() >= minSystemicTreatments ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("Patient did not receive at least " + minSystemicTreatments + " systemic treatments");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassMessages("Patient received at least " + minSystemicTreatments + " systemic treatments");
        }

        return builder.build();
    }

    @NotNull
    private static List<PriorTumorTreatment> systemicOnly(@NotNull List<PriorTumorTreatment> priorTumorTreatments) {
        List<PriorTumorTreatment> filtered = Lists.newArrayList();
        for (PriorTumorTreatment priorTumorTreatment : priorTumorTreatments) {
            if (priorTumorTreatment.isSystemic()) {
                filtered.add(priorTumorTreatment);
            }
        }
        return filtered;
    }
}
