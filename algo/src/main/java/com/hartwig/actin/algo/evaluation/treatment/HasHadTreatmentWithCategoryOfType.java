package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadTreatmentWithCategoryOfType implements PassOrFailEvaluator {

    @NotNull
    private final TreatmentCategory category;
    @NotNull
    private final String type;

    HasHadTreatmentWithCategoryOfType(@NotNull final TreatmentCategory category, @NotNull final String type) {
        this.category = category;
        this.type = type;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                if (TreatmentTypeResolver.isOfType(treatment, category, type)) {
                    return true;
                }
            }
        }

        return false;
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient has received " + type + " " + category.display() + " treatment";
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient has not received " + type + " " + category.display() + " treatment";
    }
}
