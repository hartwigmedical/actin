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
                "Undetermined HR+ HER2- breast cancer type"
            )
        }

        val expandedDoidSet = DoidEvaluationFunctions.createFullExpandedDoidTree(doidModel, tumorDoids)
        val isBreastCancer = DoidConstants.BREAST_CANCER_DOID in expandedDoidSet
        val isHer2Negative = DoidConstants.HER2_NEGATIVE_BREAST_CANCER_DOID in expandedDoidSet
        val isProgesteronePositive = DoidConstants.PROGESTERONE_POSITIVE_BREAST_CANCER_DOID in expandedDoidSet
        val isEstrogenPositive = DoidConstants.ESTROGEN_POSITIVE_BREAST_CANCER_DOID in expandedDoidSet
        val isHer2Positive = DoidConstants.HER2_POSITIVE_BREAST_CANCER_DOID in expandedDoidSet
        val isProgesteroneNegative = DoidConstants.PROGESTERONE_NEGATIVE_BREAST_CANCER_DOID in expandedDoidSet
        val isEstrogenNegative = DoidConstants.ESTROGEN_NEGATIVE_BREAST_CANCER_DOID in expandedDoidSet
        val hasERBB2Amplified = geneIsAmplifiedForPatient("ERBB2", record)

        return when {
            isHer2Negative && (isProgesteronePositive || isEstrogenPositive) -> {
                if (hasERBB2Amplified) {
                    EvaluationFactory.warn(
                        "Patient has HER2-negative hormone-positive breast cancer but with ERBB2 amplified",
                        "Undetermined HR+ HER2- breast cancer due to presence of ERBB2 gene amp"
                    )
                } else {
                    EvaluationFactory.pass("Patient has HER2-negative hormone-positive breast cancer", "Tumor type")
                }
            }

            (isProgesteronePositive || isEstrogenPositive) && !isHer2Positive -> {
                EvaluationFactory.warn(
                    "Patient has hormone-positive breast cancer but with unclear HER2 status",
                    "Undetermined HR+ HER2- breast cancer due to HER2 status"
                )
            }

            isBreastCancer && !isHer2Positive && !isEstrogenNegative && !isProgesteroneNegative -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient may have breast cancer of HR+ HER2- subtype",
                    "Undetermined if breast cancer of HR+ HER2- subtype"
                )
            }

            else -> EvaluationFactory.fail("Patient has no hormone-positive HER2-negative breast cancer", "Tumor type")
        }
    }
}