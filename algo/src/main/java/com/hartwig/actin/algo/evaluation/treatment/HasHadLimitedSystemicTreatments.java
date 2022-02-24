package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.jetbrains.annotations.NotNull;

public class HasHadLimitedSystemicTreatments implements PassOrFailEvaluator {

    private final int maxSystemicTreatments;

    HasHadLimitedSystemicTreatments(final int maxSystemicTreatments) {
        this.maxSystemicTreatments = maxSystemicTreatments;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        int systemicCount = 0;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.isSystemic()) {
                systemicCount++;
            }
        }

        return systemicCount <= maxSystemicTreatments;
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient has received at most " + maxSystemicTreatments + " systematic treatments";
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient has received more than " + maxSystemicTreatments + " systematic treatments";
    }
}
