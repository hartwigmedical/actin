package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadTreatmentWithCategoryOfType implements EvaluationFunction {

    @NotNull
    private final TreatmentCategory category;
    @NotNull
    private final String type;

    HasHadTreatmentWithCategoryOfType(@NotNull final TreatmentCategory category, @NotNull final String type) {
        this.category = category;
        this.type = type;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull final PatientRecord record) {
        boolean hasHadValidTreatment = false;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                if (TreatmentTypeResolver.isOfType(treatment, category, type)) {
                    hasHadValidTreatment = true;
                }
            }
        }

        EvaluationResult result = hasHadValidTreatment ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("Patient has not received " + type + " " + category.display() + " treatment");
        } else if (result.isPass()) {
            builder.addPassMessages("Patient has received " + type + " " + category.display() + " treatment");
        }

        return builder.build();
    }
}
