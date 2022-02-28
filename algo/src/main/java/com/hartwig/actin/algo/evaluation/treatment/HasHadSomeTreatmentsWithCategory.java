package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;

import org.jetbrains.annotations.NotNull;

public class HasHadSomeTreatmentsWithCategory implements PassOrFailEvaluator {

    @NotNull
    private final TreatmentCategory category;
    private final int minTreatmentLines;

    HasHadSomeTreatmentsWithCategory(@NotNull final TreatmentCategory category, final int minTreatmentLines) {
        this.category = category;
        this.minTreatmentLines = minTreatmentLines;
    }

    @Override
    public boolean isPass(@NotNull final PatientRecord record) {
        int numTreatmentLines = 0;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                numTreatmentLines++;
            }
        }

        return numTreatmentLines >= minTreatmentLines;
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient has received at least " + minTreatmentLines + " lines of " + display(category);
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient has not received at least " + minTreatmentLines + " lines of " + display(category);
    }

    @NotNull
    private static String display(@NotNull TreatmentCategory category) {
        return TreatmentCategoryResolver.toString(category).toLowerCase();
    }
}
