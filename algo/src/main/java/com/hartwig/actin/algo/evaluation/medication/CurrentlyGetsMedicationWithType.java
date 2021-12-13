package com.hartwig.actin.algo.evaluation.medication;

import javax.annotation.Nullable;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsMedicationWithType implements EvaluationFunction {

    @Nullable
    private final String type;

    CurrentlyGetsMedicationWithType(@Nullable final String type) {
        this.type = type;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (Medication medication : record.clinical().medications()) {
            Boolean active = medication.active();
            if (active != null && active && (type == null || medication.type().equals(type))) {
                return Evaluation.PASS;
            }
        }
        return Evaluation.FAIL;
    }
}
