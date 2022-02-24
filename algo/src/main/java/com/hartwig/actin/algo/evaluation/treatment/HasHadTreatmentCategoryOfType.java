package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasHadTreatmentCategoryOfType implements PassOrFailEvaluator {

    @NotNull
    private final TreatmentCategory category;
    @Nullable
    private final String type;

    HasHadTreatmentCategoryOfType(@NotNull final TreatmentCategory category, @Nullable final String type) {
        this.category = category;
        this.type = type;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                if (type == null || TreatmentTypeResolver.isOfType(treatment, category, type)) {
                    return true;
                }
            }
        }

        return false;
    }

    @NotNull
    @Override
    public String passMessage() {
        String categoryDisplay = TreatmentCategoryResolver.toString(category).toLowerCase();
        if (type == null) {
            return "Patient has received " + categoryDisplay;
        } else {
            return "Patient has received " + type + " " + categoryDisplay + " treatment";
        }
    }

    @NotNull
    @Override
    public String failMessage() {
        String categoryDisplay = TreatmentCategoryResolver.toString(category).toLowerCase();
        if (type == null) {
            return "Patient has not received " + categoryDisplay;
        } else {
            return "Patient has not received " + type + " " + categoryDisplay + " treatment";
        }
    }
}
