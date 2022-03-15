package com.hartwig.actin.algo.evaluation.treatment;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadTreatmentWithCategoryButNotOfTypes implements EvaluationFunction {

    @NotNull
    private final TreatmentCategory category;
    @NotNull
    private final List<String> ignoreTypes;

    HasHadTreatmentWithCategoryButNotOfTypes(@NotNull final TreatmentCategory category, @NotNull final List<String> ignoreTypes) {
        this.category = category;
        this.ignoreTypes = ignoreTypes;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasHadValidTreatment = false;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                boolean hasCorrectType = true;
                for (String ignoreType : ignoreTypes) {
                    if (TreatmentTypeResolver.isOfType(treatment, category, ignoreType)) {
                        hasCorrectType = false;
                    }
                }
                if (hasCorrectType) {
                    hasHadValidTreatment = true;
                }
            }
        }

        EvaluationResult result = hasHadValidTreatment ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has not received " + category.display() + ", ignoring " + Format.concat(ignoreTypes));
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient received " + category.display() + ", ignoring " + Format.concat(ignoreTypes));
        }

        return builder.build();
    }
}
