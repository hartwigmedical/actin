package com.hartwig.actin.algo.evaluation.medication;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsMedicationWithName implements PassOrFailEvaluator {

    @NotNull
    private final String termToFind;

    CurrentlyGetsMedicationWithName(@NotNull final String termToFind) {
        this.termToFind = termToFind;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        for (Medication medication : MedicationFilter.active(record.clinical().medications())) {
            if (medication.name().toLowerCase().contains(termToFind.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient currently gets medication with name " + termToFind;
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient currently does not get medication with name " + termToFind;
    }
}
