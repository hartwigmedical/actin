package com.hartwig.actin.algo.evaluation.treatment;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadTreatmentWithCategoryOfTypes implements EvaluationFunction {

    @NotNull
    private final TreatmentCategory category;
    @NotNull
    private final List<String> types;

    HasHadTreatmentWithCategoryOfTypes(@NotNull final TreatmentCategory category, @NotNull final List<String> types) {
        this.category = category;
        this.types = types;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasHadValidTreatment = false;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (hasValidCategoryAndType(treatment)) {
                hasHadValidTreatment = true;
            }
        }

        EvaluationResult result = hasHadValidTreatment ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has not received " + Format.concat(types) + " " + category.display() + " treatment");
            builder.addFailGeneralMessages("No " + category.display() + " treatment");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has received " + Format.concat(types) + " " + category.display() + " treatment");
            builder.addPassGeneralMessages(category.display() + " treatment");
        }

        return builder.build();
    }

    private boolean hasValidCategoryAndType(@NotNull PriorTumorTreatment treatment) {
        if (treatment.categories().contains(category)) {
            for (String type : types) {
                if (TreatmentTypeResolver.isOfType(treatment, category, type)) {
                    return true;
                }
            }
        }
        return false;
    }
}
