package com.hartwig.actin.algo.evaluation.treatment;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasLimitedCumulativeAnthracyclineExposure implements EvaluationFunction {

    static final String ANTHRACYCLINE_CHEMO_TYPE = "Anthracycline";

    static final Set<String> CANCER_DOIDS_FOR_ANTHRACYCLINE = Sets.newHashSet();
    static final Set<String> PRIOR_PRIMARY_SUSPICIOUS_TREATMENTS = Sets.newHashSet();

    static {
        CANCER_DOIDS_FOR_ANTHRACYCLINE.add("1612"); // breast cancer
        CANCER_DOIDS_FOR_ANTHRACYCLINE.add("10619"); // lymph node cancer
        CANCER_DOIDS_FOR_ANTHRACYCLINE.add("1115"); // sarcoma
        CANCER_DOIDS_FOR_ANTHRACYCLINE.add("2394"); // ovarian cancer
        CANCER_DOIDS_FOR_ANTHRACYCLINE.add("9538"); // multiple myeloma
        CANCER_DOIDS_FOR_ANTHRACYCLINE.add("1240"); // leukemia

        PRIOR_PRIMARY_SUSPICIOUS_TREATMENTS.add("chemotherapy");
        PRIOR_PRIMARY_SUSPICIOUS_TREATMENTS.add("anthracycline");
    }

    @NotNull
    private final DoidModel doidModel;

    HasLimitedCumulativeAnthracyclineExposure(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasSuspectPriorTumorWithSuspectTreatmentHistory = false;
        for (PriorSecondPrimary priorSecondPrimary : record.clinical().priorSecondPrimaries()) {
            if (hasSuspiciousCancerType(priorSecondPrimary.doids())
                    && hasSuspiciousTreatmentHistory(priorSecondPrimary.treatmentHistory())) {
                hasSuspectPriorTumorWithSuspectTreatmentHistory = true;
            }
        }

        boolean hasSuspectPrimaryTumor = hasSuspiciousCancerType(record.clinical().tumor().doids());
        boolean hasAnthracyclineChemo = false;
        boolean hasChemoWithoutType = false;
        for (PriorTumorTreatment priorTumorTreatment : record.clinical().priorTumorTreatments()) {
            if (TreatmentTypeResolver.isOfType(priorTumorTreatment, TreatmentCategory.CHEMOTHERAPY, ANTHRACYCLINE_CHEMO_TYPE)) {
                hasAnthracyclineChemo = true;
            } else if (priorTumorTreatment.categories().contains(TreatmentCategory.CHEMOTHERAPY)
                    && !TreatmentTypeResolver.hasTypeConfigured(priorTumorTreatment, TreatmentCategory.CHEMOTHERAPY)) {
                hasChemoWithoutType = true;
            }
        }

        if (hasAnthracyclineChemo) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Patient has received anthracycline chemotherapy, exact dosage cannot be determined")
                    .addUndeterminedGeneralMessages("Anthracycline exposure")
                    .build();
        } else if (hasChemoWithoutType && hasSuspectPrimaryTumor) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "Patient has cancer type that is associated with potential anthracycline chemotherapy, "
                                    + "undetermined if anthracycline chemotherapy has been given")
                    .addUndeterminedGeneralMessages("Anthracycline exposure")
                    .build();
        } else if (hasSuspectPriorTumorWithSuspectTreatmentHistory) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "Patient has had a prior tumor that is associated with potential anthracycline chemotherapy")
                    .addUndeterminedGeneralMessages("Anthracycline exposure")
                    .build();
        }

        return EvaluationFactory.unrecoverable().

                result(EvaluationResult.PASS).

                addPassSpecificMessages(
                        "Patient should not have been exposed to anthracycline chemotherapy, thus not exceeding maximum dose").

                addPassGeneralMessages("Anthracycline exposure").

                build();

    }

    private boolean hasSuspiciousCancerType(@Nullable Set<String> tumorDoids) {
        if (tumorDoids == null) {
            return false;
        }

        for (String tumorDoid : tumorDoids) {
            Set<String> doidWithParents = doidModel.doidWithParents(tumorDoid);
            for (String suspiciousDoid : CANCER_DOIDS_FOR_ANTHRACYCLINE) {
                if (doidWithParents.contains(suspiciousDoid)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean hasSuspiciousTreatmentHistory(@NotNull String priorPrimaryTreatmentHistory) {
        if (priorPrimaryTreatmentHistory.isEmpty()) {
            return true;
        }

        String lowerCaseTreatmentHistory = priorPrimaryTreatmentHistory.toLowerCase();
        for (String suspiciousTreatment : PRIOR_PRIMARY_SUSPICIOUS_TREATMENTS) {
            if (lowerCaseTreatmentHistory.contains(suspiciousTreatment.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

}
