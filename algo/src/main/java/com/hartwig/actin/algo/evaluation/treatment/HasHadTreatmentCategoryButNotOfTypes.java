package com.hartwig.actin.algo.evaluation.treatment;

import java.util.List;
import java.util.StringJoiner;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;

import org.jetbrains.annotations.NotNull;

public class HasHadTreatmentCategoryButNotOfTypes implements EvaluationFunction {

    @NotNull
    private final TreatmentCategory category;
    @NotNull
    private final List<String> ignoreTypes;

    HasHadTreatmentCategoryButNotOfTypes(@NotNull final TreatmentCategory category, @NotNull final List<String> ignoreTypes) {
        this.category = category;
        this.ignoreTypes = ignoreTypes;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasHadTreatmentWithCategoryButNotOfType = false;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                boolean hasCorrectType = true;
                for (String ignoreType : ignoreTypes) {
                    if (TreatmentTypeResolver.isOfType(treatment, category, ignoreType)) {
                        hasCorrectType = false;
                    }
                }
                if (hasCorrectType) {
                    hasHadTreatmentWithCategoryButNotOfType = true;
                }
            }
        }

        EvaluationResult result = hasHadTreatmentWithCategoryButNotOfType ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        String categoryDisplay = TreatmentCategoryResolver.toString(category).toLowerCase();
        String types = concat(ignoreTypes);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("Patient has not received " + categoryDisplay + ", ignoring " + types);
        } else {
            builder.addPassMessages("Patient received " + categoryDisplay + ", ignoring " + types);
        }
        return builder.build();
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
