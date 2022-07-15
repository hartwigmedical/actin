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

    static {
        CANCER_DOIDS_FOR_ANTHRACYCLINE.add("1612"); // breast cancer
        CANCER_DOIDS_FOR_ANTHRACYCLINE.add("10619"); // lymph node cancer
        CANCER_DOIDS_FOR_ANTHRACYCLINE.add("1115"); // sarcoma
        CANCER_DOIDS_FOR_ANTHRACYCLINE.add("2394"); // ovarian cancer
        CANCER_DOIDS_FOR_ANTHRACYCLINE.add("9538"); // multiple myeloma
        CANCER_DOIDS_FOR_ANTHRACYCLINE.add("1240"); // leukemia
    }

    @NotNull
    private final DoidModel doidModel;

    HasLimitedCumulativeAnthracyclineExposure(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasSuspectPrimaryTumor = hasSuspiciousCancerType(record.clinical().tumor().doids());
        boolean hasSuspectPriorTumor = false;
        for (PriorSecondPrimary priorSecondPrimary : record.clinical().priorSecondPrimaries()) {
            if (hasSuspiciousCancerType(priorSecondPrimary.doids())) {
                hasSuspectPriorTumor = true;
            }
        }

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
                    .addUndeterminedSpecificMessages("Patient has received anthracycline chemotherapy")
                    .addUndeterminedGeneralMessages("Anthracycline exposure")
                    .build();
        } else if (hasChemoWithoutType) {
            if (hasSuspectPrimaryTumor) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.UNDETERMINED)
                        .addUndeterminedSpecificMessages("Patient has cancer type that is associated with anthracycline chemotherapy")
                        .addUndeterminedGeneralMessages("Anthracycline exposure")
                        .build();
            } else if (hasSuspectPriorTumor) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.UNDETERMINED)
                        .addUndeterminedSpecificMessages("Patient has had prior tumor that is associated with anthracycline chemotherapy")
                        .addUndeterminedGeneralMessages("Anthracycline exposure")
                        .build();
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has not been exposed to anthracycline chemotherapy")
                .addFailGeneralMessages("Anthracycline exposure")
                .build();
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
}
