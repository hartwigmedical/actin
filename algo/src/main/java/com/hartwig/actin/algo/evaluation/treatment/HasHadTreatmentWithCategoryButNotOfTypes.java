package com.hartwig.actin.algo.evaluation.treatment;

import java.util.List;
import java.util.StringJoiner;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadTreatmentWithCategoryButNotOfTypes implements PassOrFailEvaluator {

    @NotNull
    private final TreatmentCategory category;
    @NotNull
    private final List<String> ignoreTypes;

    HasHadTreatmentWithCategoryButNotOfTypes(@NotNull final TreatmentCategory category, @NotNull final List<String> ignoreTypes) {
        this.category = category;
        this.ignoreTypes = ignoreTypes;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                boolean hasCorrectType = true;
                for (String ignoreType : ignoreTypes) {
                    if (TreatmentTypeResolver.isOfType(treatment, category, ignoreType)) {
                        hasCorrectType = false;
                    }
                }
                if (hasCorrectType) {
                    return true;
                }
            }
        }

        return false;
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient received " + category.display() + ", ignoring " + concat(ignoreTypes);
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient has not received " + category.display() + ", ignoring " + concat(ignoreTypes);
    }

    @NotNull
    private static String concat(@NotNull List<String> strings) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String string : strings) {
            joiner.add(string);
        }
        return joiner.toString();
    }
}
