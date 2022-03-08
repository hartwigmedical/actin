package com.hartwig.actin.algo.evaluation.medication;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsAnyMedication implements PassOrFailEvaluator {

    CurrentlyGetsAnyMedication() {
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        return !MedicationFilter.active(record.clinical().medications()).isEmpty();
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient currently receives medication with status 'active'";
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient does not currently receive medication with status 'active'";
    }
}
