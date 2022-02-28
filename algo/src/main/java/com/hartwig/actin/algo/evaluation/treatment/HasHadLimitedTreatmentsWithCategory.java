package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadLimitedTreatmentsWithCategory implements PassOrFailEvaluator {

    @NotNull
    private final TreatmentCategory category;
    private final int maxTreatmentLines;

    HasHadLimitedTreatmentsWithCategory(@NotNull final TreatmentCategory category, final int maxTreatmentLines) {
        this.category = category;
        this.maxTreatmentLines = maxTreatmentLines;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        int numTreatmentLines = 0;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                numTreatmentLines++;
            }
        }

        return numTreatmentLines <= maxTreatmentLines;
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient has received at most " + maxTreatmentLines + " lines of " + category.display();
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient has received more than " + maxTreatmentLines + " lines of " + category.display();
    }
}
