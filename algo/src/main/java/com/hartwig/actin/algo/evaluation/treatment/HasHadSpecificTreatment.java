package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.jetbrains.annotations.NotNull;

public class HasHadSpecificTreatment implements PassOrFailEvaluator {

    @NotNull
    private final String name;

    HasHadSpecificTreatment(@NotNull final String name) {
        this.name = name;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.name().toLowerCase().contains(name.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient has received " + name + " treatment";
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient has not received " + name + " treatment";
    }
}
