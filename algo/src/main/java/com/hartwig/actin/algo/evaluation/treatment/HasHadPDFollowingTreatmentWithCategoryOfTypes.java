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
        }

        EvaluationResult result;
        if (hasHadTreatmentWithPD) {
            result = EvaluationResult.PASS;
        } else if (hasHadTreatmentWithUnclearStopReason || hasPotentiallyHadTreatment) {
            result = EvaluationResult.UNDETERMINED;
        } else {
            result = EvaluationResult.FAIL;
        }

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            if (hasHadTreatment) {
                builder.addFailSpecificMessages("Patient has received " + treatment() + " but not with stop reason PD");
            } else {
                builder.addFailSpecificMessages("No " + category.display() + " treatment");
            }
            builder.addFailGeneralMessages("Systemic treatments");
        } else if (result == EvaluationResult.UNDETERMINED) {
            if (hasPotentiallyHadTreatment) {
                builder.addUndeterminedSpecificMessages("Can't determine whether patient has received " + treatment());
            } else {
                builder.addUndeterminedSpecificMessages("Patient has received " + treatment() + " but with unclear stop reason");
            }
            builder.addUndeterminedGeneralMessages("Unclear " + category.display() + " treatment");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has received " + treatment() + " with stop reason PD");
            builder.addPassGeneralMessages(category.display() + " treatment");
        }

        return builder.build();
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
