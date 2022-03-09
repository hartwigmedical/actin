package com.hartwig.actin.algo.evaluation.treatment;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadLimitedTreatmentsWithCategoryOfTypes implements PassOrFailEvaluator {

    @NotNull
    private final TreatmentCategory category;
    @NotNull
    private final List<String> types;
    private final int maxTreatmentLines;

    HasHadLimitedTreatmentsWithCategoryOfTypes(@NotNull final TreatmentCategory category, @NotNull final List<String> types,
            final int maxTreatmentLines) {
        this.category = category;
        this.types = types;
        this.maxTreatmentLines = maxTreatmentLines;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        int numTreatmentLines = 0;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                boolean matchWithAnyType = false;
                for (String type : types) {
                    if (TreatmentTypeResolver.isOfType(treatment, category, type)) {
                        matchWithAnyType = true;
                    }
                }
                if (matchWithAnyType) {
                    numTreatmentLines++;
                }
            }
        }

        return numTreatmentLines <= maxTreatmentLines;
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient has received at most " + maxTreatmentLines + " lines of " + Format.concat(types) + " " + category.display();
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient has not received at most " + maxTreatmentLines + " lines of " + Format.concat(types) + " " + category.display();
    }
}
