package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.treatmentResultedInPDOption;

import java.util.Optional;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadPDFollowingTreatmentWithCategory implements EvaluationFunction {

    @NotNull
    private final TreatmentCategory category;

    HasHadPDFollowingTreatmentWithCategory(@NotNull final TreatmentCategory category) {
        this.category = category;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasHadPDFollowingTreatmentWithCategory = false;
        boolean hasHadTreatmentWithUnclearPDStatus = false;
        boolean hasHadTrial = false;

        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                Optional<Boolean> treatmentResultedInPDOption = treatmentResultedInPDOption(treatment);
                if (treatmentResultedInPDOption.orElse(false)) {
                    hasHadPDFollowingTreatmentWithCategory = true;
                } else if (treatmentResultedInPDOption.isEmpty()) {
                    hasHadTreatmentWithUnclearPDStatus = true;
                }
            }

            if (treatment.categories().contains(TreatmentCategory.TRIAL)) {
                hasHadTrial = true;
            }
        }

        if (hasHadPDFollowingTreatmentWithCategory) {
            return EvaluationFactory.pass("Patient has had progressive disease following treatment with category " + category.display(),
                    category.display() + " treatment with PD");
        } else if (hasHadTreatmentWithUnclearPDStatus) {
            return EvaluationFactory.undetermined(
                    "Patient has had treatment with category " + category.display() + " but unclear PD status",
                    "Had " + category.display() + " treatment with unclear PD status");
        } else if (hasHadTrial) {
            return EvaluationFactory.undetermined("Patient has had trial with unclear treatment category", "Trial treatment");
        } else {
            return EvaluationFactory.fail("Patient has no progressive disease following treatment with category " + category.display(),
                    "No " + category.display() + " treatment with PD");
        }

    }
}
