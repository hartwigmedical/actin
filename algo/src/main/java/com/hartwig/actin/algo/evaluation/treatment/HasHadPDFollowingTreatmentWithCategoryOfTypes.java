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

public class HasHadPDFollowingTreatmentWithCategoryOfTypes implements EvaluationFunction {

    static final String STOP_REASON_PD = "PD";

    @NotNull
    private final TreatmentCategory category;
    @NotNull
    private final List<String> types;

    HasHadPDFollowingTreatmentWithCategoryOfTypes(@NotNull final TreatmentCategory category, @NotNull final List<String> types) {
        this.category = category;
        this.types = types;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasHadTreatment = false;
        boolean hasPotentiallyHadTreatment = false;
        boolean hasHadTreatmentWithPD = false;
        boolean hasHadTreatmentWithUnclearStopReason = false;
        boolean hasHadTrial = false;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                if (hasValidType(treatment)) {
                    hasHadTreatment = true;
                    String stopReason = treatment.stopReason();

                    if (stopReason != null) {
                        if (stopReason.equalsIgnoreCase(STOP_REASON_PD)) {
                            hasHadTreatmentWithPD = true;
                        }
                    } else {
                        hasHadTreatmentWithUnclearStopReason = true;
                    }
                } else if (!TreatmentTypeResolver.hasTypeConfigured(treatment, category)) {
                    hasPotentiallyHadTreatment = true;
                }
            }

            if (treatment.categories().contains(TreatmentCategory.TRIAL)) {
                hasHadTrial = true;
            }
        }

        if (hasHadTreatmentWithPD) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has received " + treatment() + " with stop reason PD")
                    .addPassGeneralMessages(category.display() + " treatment with PD")
                    .build();
        } else if (hasHadTreatmentWithUnclearStopReason) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Patient has received " + treatment() + " but with undetermined stop reason")
                    .build();
        } else if (hasPotentiallyHadTreatment || hasHadTrial) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Undetermined whether patient has received " + treatment())
                    .build();
        } else if (hasHadTreatment) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Patient has received " + treatment() + " but not with stop reason PD")
                    .addFailGeneralMessages("Systemic treatments")
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("No " + category.display() + " treatment with PD")
                    .addFailGeneralMessages("Systemic treatments")
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

    @NotNull
    private String treatment() {
        return Format.concat(types) + " " + category.display() + " treatment";
    }
}
