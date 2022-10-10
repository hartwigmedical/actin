package com.hartwig.actin.algo.evaluation.treatment;

import java.util.List;

import javax.annotation.Nullable;

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

public class HasHadPDFollowingTreatmentWithCategoryOfTypesAndCycles implements EvaluationFunction {

    static final String STOP_REASON_PD = "PD";

    @NotNull
    private final TreatmentCategory category;
    @NotNull
    private final List<String> types;
    @Nullable
    private final Integer minCycles;

    HasHadPDFollowingTreatmentWithCategoryOfTypesAndCycles(@NotNull final TreatmentCategory category, @NotNull final List<String> types,
            @Nullable final Integer minCycles) {
        this.category = category;
        this.types = types;
        this.minCycles = minCycles;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasHadTreatment = false;
        boolean hasPotentiallyHadTreatment = false;
        boolean hasHadTreatmentWithPDAndCycles = false;
        boolean hasHadTreatmentWithUnclearStopReason = false;
        boolean hasHadTreatmentWithUnclearCycles = false;
        boolean hasHadTrial = false;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                if (hasValidType(treatment)) {
                    hasHadTreatment = true;
                    String stopReason = treatment.stopReason();
                    Integer cycles = treatment.cycles();

                    if (stopReason != null && (minCycles == null || cycles != null)) {
                        if (stopReason.equalsIgnoreCase(STOP_REASON_PD) && (minCycles == null || cycles >= minCycles)) {
                            hasHadTreatmentWithPDAndCycles = true;
                        }
                    } else {
                        if (stopReason == null) {
                            hasHadTreatmentWithUnclearStopReason = true;
                        }
                        if (minCycles != null && cycles == null) {
                            hasHadTreatmentWithUnclearCycles = true;
                        }
                    }
                } else if (!TreatmentTypeResolver.hasTypeConfigured(treatment, category)) {
                    hasPotentiallyHadTreatment = true;
                }
            }

            if (treatment.categories().contains(TreatmentCategory.TRIAL)) {
                hasHadTrial = true;
            }
        }

        if (hasHadTreatmentWithPDAndCycles) {
            ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(EvaluationResult.PASS);
            if (minCycles == null) {
                return builder.addPassSpecificMessages("Patient has received " + treatment() + " with stop reason PD")
                        .addPassGeneralMessages(category.display() + " treatment with PD")
                        .build();
            } else {
                return builder.addPassSpecificMessages(
                                "Patient has received " + treatment() + " with stop reason PD and at least " + minCycles + " cycles")
                        .addPassGeneralMessages(category.display() + " treatment with PD and sufficient cycles")
                        .build();
            }
        } else if (hasHadTreatmentWithUnclearStopReason) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Patient has received " + treatment() + " but with undetermined stop reason")
                    .addUndeterminedGeneralMessages(category.display() + " undetermined stop reason")
                    .build();
        } else if (hasHadTreatmentWithUnclearCycles) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Patient has received " + treatment() + " but with unclear number of cycles")
                    .addUndeterminedGeneralMessages(category.display() + " undetermined nr of cycles")
                    .build();
        } else if (hasPotentiallyHadTreatment || hasHadTrial) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Undetermined whether patient has received " + treatment())
                    .addUndeterminedGeneralMessages("Undetermined received" + category.display())
                    .build();
        } else if (hasHadTreatment) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Patient has received " + treatment() + " but not with stop reason PD")
                    .addFailGeneralMessages("No PD after " + category.display())
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("No " + treatment() + " treatment with PD")
                    .addFailGeneralMessages("No " + category.display())
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
