package com.hartwig.actin.algo.evaluation.treatment;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
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
    public Evaluation evaluate(@NotNull final PatientRecord record) {
        List<PriorTumorTreatment> systemicOnly = systemicOnly(record.clinical().priorTumorTreatments());

        return systemicOnly.size() <= maxSystemicTreatments ? Evaluation.PASS : Evaluation.FAIL;
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
