package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasProstateCancerWithSmallCellHistology implements EvaluationFunction {

    static final String PROSTATE_CANCER_DOID = "10283";
    static final String PROSTATE_SMALL_CELL_CARCINOMA = "7141";

    @NotNull
    private final DoidModel doidModel;

    HasProstateCancerWithSmallCellHistology(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> patientDoids = record.clinical().tumor().doids();
        if (patientDoids == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Could not determine whether patient has prostate cancer with small cell histology")
                    .addUndeterminedGeneralMessages("Tumor type")
                    .build();
        }

        boolean hasValidDoidMatch = false;
        boolean hasApproximateDoidMatch = false;

        for (String doid : patientDoids) {
            Set<String> expanded = doidModel.doidWithParents(doid);
            if (expanded.contains(PROSTATE_CANCER_DOID)) {
                String extraDetails = record.clinical().tumor().primaryTumorExtraDetails();
                boolean hasSmallCellHistology = (extraDetails != null && extraDetails.toLowerCase().contains("small cell"));
                if (expanded.contains(PROSTATE_SMALL_CELL_CARCINOMA) || hasSmallCellHistology) {
                    hasValidDoidMatch = true;
                }
            }

            if (doid.equals(PROSTATE_CANCER_DOID)) {
                hasApproximateDoidMatch = true;
            }
        }

        if (hasValidDoidMatch) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassGeneralMessages("Patient has prostate cancer with small cell histology")
                    .addPassSpecificMessages("Tumor type")
                    .build();
        } else if (hasApproximateDoidMatch) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnGeneralMessages("Patient has prostate cancer but with no configured histology subtype")
                    .addWarnSpecificMessages("Tumor type")
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Patient has no prostate cancer with small cell histology")
                    .addFailGeneralMessages("Tumor type")
                    .build();
        }
    }
}
