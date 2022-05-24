package com.hartwig.actin.algo.evaluation.treatment;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
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
        boolean hasPotentiallyHadValidTreatment = false;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                if (hasValidType(treatment)) {
                    hasHadValidTreatment = true;
                } else if (!TreatmentTypeResolver.hasTypeConfigured(treatment, category)) {
                    hasPotentiallyHadValidTreatment = true;
                }
            }
        }

        if (hasHadValidTreatment) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has received " + Format.concat(types) + " " + category.display() + " treatment")
                    .addPassGeneralMessages(category.display() + " treatment")
                    .build();
        } else if (hasPotentiallyHadValidTreatment) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "Can't determine whether patient has received " + Format.concat(types) + " " + category.display()
                                    + " treatment")
                    .addUndeterminedGeneralMessages("Unclear " + category.display() + " treatment")
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Patient has not received " + Format.concat(types) + " " + category.display() + " treatment")
                    .addFailGeneralMessages("No " + category.display() + " treatment")
                    .build();
        }
    }

    private boolean hasValidType(@NotNull PriorTumorTreatment treatment) {
        for (String type : types) {
            if (TreatmentTypeResolver.isOfType(treatment, category, type)) {
                return true;
            }
        }
        return false;
    }
}
