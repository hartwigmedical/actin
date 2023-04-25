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
        boolean hasHadOtherTrial = false;
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
            } else if (treatment.categories().contains(TreatmentCategory.TRIAL)) {
                hasHadOtherTrial = true;
            }
        }

        if (hasHadValidTreatment) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient received " + category.display() + ", ignoring " + Format.concat(ignoreTypes))
                    .addPassGeneralMessages("Received " + category.display() + ", ignoring " + Format.concat(ignoreTypes))
                    .build();
        } else if (hasHadOtherTrial) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "Patient may have received " + category.display() + " in a trial, ignoring " + Format.concat(ignoreTypes))
                    .addUndeterminedGeneralMessages(
                            "Undetermined if received " + category.display() + ", ignoring " + Format.concat(ignoreTypes))
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Patient has not received " + category.display() + ", ignoring " + Format.concat(ignoreTypes))
                    .addFailGeneralMessages("Not received " + category.display() + ", ignoring " + Format.concat(ignoreTypes))
                    .build();
        }
    }
}
