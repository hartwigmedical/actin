package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadLimitedTreatmentsWithCategoryOfType implements PassOrFailEvaluator {

    @NotNull
    private final TreatmentCategory category;
    @NotNull
    private final String type;
    private final int maxTreatmentLines;

    HasHadLimitedTreatmentsWithCategoryOfType(@NotNull final TreatmentCategory category, @NotNull final String type,
            final int maxTreatmentLines) {
        this.category = category;
        this.type = type;
        this.maxTreatmentLines = maxTreatmentLines;
    }

    @Override
    public boolean isPass(@NotNull final PatientRecord record) {
        int numTreatmentLines = 0;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category) && TreatmentTypeResolver.isOfType(treatment, category, type)) {
                numTreatmentLines++;
            }
        }

        return numTreatmentLines <= maxTreatmentLines;
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient has received at most " + maxTreatmentLines + " lines of " + type + " " + category.display();
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient has not received at most " + maxTreatmentLines + " lines of " + type + " " + category.display();
    }
}
