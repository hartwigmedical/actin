package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator.geneIsAmplifiedForPatient
import com.hartwig.actin.doid.DoidModel

class HasBreastCancerHormonePositiveHER2Negative internal constructor(private val doidModel: DoidModel) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.clinical().tumor().doids()
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined(
                "Could not determine whether patient has hormone-positive HER2-negative breast cancer",
                "Undetermined HR+ HR- breast cancer type"
            )
        }
        val isBreastCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.BREAST_CANCER_DOID)
        val isHer2Negative = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.HER2_NEGATIVE_BREAST_CANCER_DOID)
        val isProgesteronePositive =
            DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.PROGESTERONE_POSITIVE_BREAST_CANCER_DOID)
        val isEstrogenPositive =
            DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.ESTROGEN_POSITIVE_BREAST_CANCER_DOID)
        val isHer2Positive = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.HER2_POSITIVE_BREAST_CANCER_DOID)
        val isProgesteroneNegative =
            DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.PROGESTERONE_NEGATIVE_BREAST_CANCER_DOID)
        val isEstrogenNegative =
            DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.ESTROGEN_NEGATIVE_BREAST_CANCER_DOID)
        val hasHer2Amplified = geneIsAmplifiedForPatient("ERBB2", record)
        return when {
            isHer2Negative && (isProgesteronePositive || isEstrogenPositive) -> {
                if (hasHer2Amplified) {
                    EvaluationFactory.warn(
                        "Patient has HER2-negative hormone-positive breast cancer but with HER2 amplified",
                        "Unclear HER2 status"
                    )
                } else {
                    EvaluationFactory.pass("Patient has her2-negative hormone-positive breast cancer", "Tumor type")
                }
            }

            (isProgesteronePositive || isEstrogenPositive) && !isHer2Positive -> {
                EvaluationFactory.warn(
                    "Patient has hormone-positive breast cancer but with unclear HER2 status",
                    "Unclear HER2 status"
                )
            }

            isBreastCancer && !isHer2Positive && !isEstrogenNegative && !isProgesteroneNegative -> {
                EvaluationFactory.undetermined("Patient has breast cancer but with unclear sub-type", "Unclear breast cancer type")
            }

            else -> EvaluationFactory.fail("Patient has no HER2-negative hormone-positive breast cancer", "Tumor type")
        }
    }
}