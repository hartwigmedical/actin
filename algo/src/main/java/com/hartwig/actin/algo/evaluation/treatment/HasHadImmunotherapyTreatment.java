package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadImmunotherapyTreatment implements PassOrFailEvaluator {

    HasHadImmunotherapyTreatment() {
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(TreatmentCategory.IMMUNOTHERAPY)) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient has received immunotherapy";
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient has not received immunotherapy";
    }
}
