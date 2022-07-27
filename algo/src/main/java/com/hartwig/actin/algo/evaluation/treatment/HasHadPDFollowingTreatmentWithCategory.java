package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadPDFollowingTreatmentWithCategory implements EvaluationFunction {

    static final String STOP_REASON_PD = "PD";

    @NotNull
    private final TreatmentCategory category;

    HasHadPDFollowingTreatmentWithCategory(@NotNull final TreatmentCategory category) {
        this.category = category;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasHadPDFollowingTreatmentWithCategory = false;
        boolean hasHadTreatmentWithUnclearStopReason = false;

        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                String stopReason = treatment.stopReason();

                if (stopReason != null) {
                    if (stopReason.equalsIgnoreCase(STOP_REASON_PD)) {
                        hasHadPDFollowingTreatmentWithCategory = true;
                    }
                } else {
                    hasHadTreatmentWithUnclearStopReason = true;
                }
            }
        }

        if (hasHadPDFollowingTreatmentWithCategory) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has had progressive disease following treatment with category " + category.display())
                    .addPassGeneralMessages(category.display() + " treatment")
                    .build();
        } else if (hasHadTreatmentWithUnclearStopReason) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "Patient has had treatment with category " + category.display() + " but with unknown stop reason")
                    .addUndeterminedGeneralMessages("Unclear " + category.display() + " treatment")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has no progressive disease following treatment with category " + category.display())
                .addFailGeneralMessages("No " + category.display() + " treatment")
                .build();
    }
}
