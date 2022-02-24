package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.jetbrains.annotations.NotNull;

public class HasHadSomeSystemicTreatments implements PassOrFailEvaluator {

    private final int minSystemicTreatments;

    HasHadSomeSystemicTreatments(final int minSystemicTreatments) {
        this.minSystemicTreatments = minSystemicTreatments;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        int systemicCount = 0;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.isSystemic()) {
                systemicCount++;
            }
        }

        return systemicCount >= minSystemicTreatments;
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient received at least " + minSystemicTreatments + " systemic treatments";
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient did not receive at least " + minSystemicTreatments + " systemic treatments";
    }
}
