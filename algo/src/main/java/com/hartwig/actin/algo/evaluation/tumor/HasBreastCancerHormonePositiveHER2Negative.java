package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;

import org.jetbrains.annotations.NotNull;

//TODO: Implement according to README
public class HasBreastCancerHormonePositiveHER2Negative implements EvaluationFunction {

    static final String BREAST_CANCER_DOID = "1612";

    static final String HER2_NEGATIVE_BREAST_CANCER_DOID = "0060080";
    static final String PROGESTERONE_POSITIVE_BREAST_CANCER_DOID = "0060077";
    static final String ESTROGEN_POSITIVE_BREAST_CANCER_DOID = "0060075";

    static final String HER2_POSITIVE_BREAST_CANCER_DOID = "0060079";
    static final String PROGESTERONE_NEGATIVE_BREAST_CANCER_DOID = "0060078";
    static final String ESTROGEN_NEGATIVE_BREAST_CANCER_DOID = "0060076";

    @NotNull
    private final DoidModel doidModel;

    HasBreastCancerHormonePositiveHER2Negative(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> patientDoids = record.clinical().tumor().doids();
        if (patientDoids == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Could not determine whether patient has hormone-positive HER2-negative breast cancer")
                    .addUndeterminedGeneralMessages("Tumor type")
                    .build();
        }

        boolean isBreastCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, patientDoids, BREAST_CANCER_DOID);

        boolean isHer2Negative = DoidEvaluationFunctions.isOfDoidType(doidModel, patientDoids, HER2_NEGATIVE_BREAST_CANCER_DOID);
        boolean isProgesteronePositive =
                DoidEvaluationFunctions.isOfDoidType(doidModel, patientDoids, PROGESTERONE_POSITIVE_BREAST_CANCER_DOID);
        boolean isEstrogenPositive = DoidEvaluationFunctions.isOfDoidType(doidModel, patientDoids, ESTROGEN_POSITIVE_BREAST_CANCER_DOID);

        boolean isHer2Positive = DoidEvaluationFunctions.isOfDoidType(doidModel, patientDoids, HER2_POSITIVE_BREAST_CANCER_DOID);
        boolean isProgesteroneNegative =
                DoidEvaluationFunctions.isOfDoidType(doidModel, patientDoids, PROGESTERONE_NEGATIVE_BREAST_CANCER_DOID);
        boolean isEstrogenNegative = DoidEvaluationFunctions.isOfDoidType(doidModel, patientDoids, ESTROGEN_NEGATIVE_BREAST_CANCER_DOID);

        boolean hasHer2Amplified = hasHer2Amplified(record.molecular());

        if (isHer2Negative && (isProgesteronePositive || isEstrogenPositive)) {
            if (hasHer2Amplified) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.WARN)
                        .addWarnSpecificMessages("Patient has her2-negative hormone-positive breast cancer but with HER2 amplified")
                        .addWarnGeneralMessages("Inconsistent her2 status")
                        .build();
            } else {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages("Patient has her2-negative hormone-positive breast cancer")
                        .addPassGeneralMessages("Tumor type")
                        .build();
            }
        }

        if ((isProgesteronePositive || isEstrogenPositive) && !isHer2Negative && !isHer2Positive) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("Patient has hormone-positive breast cancer but with unclear her2 status")
                    .addWarnGeneralMessages("Unclear her2 status")
                    .build();
        }

        if (isBreastCancer && !isHer2Positive && !isEstrogenNegative && !isProgesteroneNegative) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Patient has breast cancer but with unclear sub-type")
                    .addUndeterminedGeneralMessages("Unclear breast cancer type")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has no her2-negative hormone-positive breast cancer")
                .addFailGeneralMessages("Tumor type")
                .build();
    }

    private static boolean hasHer2Amplified(@NotNull MolecularRecord molecular) {
        for (Amplification amplification : molecular.drivers().amplifications()) {
            if (amplification.gene().equals("ERBB2")) {
                return true;
            }
        }
        return false;
    }
}