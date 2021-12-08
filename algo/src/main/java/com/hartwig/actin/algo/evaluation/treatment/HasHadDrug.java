package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.jetbrains.annotations.NotNull;

public class HasHadDrug implements EvaluationFunction {

    @NotNull
    private final String drug;

    HasHadDrug(@NotNull final String drug) {
        this.drug = drug;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.name().equals(drug)) {
                return Evaluation.PASS;
            }
        }

        return Evaluation.FAIL;
    }
}
