package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator.geneIsAmplifiedForPatient
import com.hartwig.actin.doid.DoidModel

class HasBreastCancerHormonePositiveHER2Negative (private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val tumorDoids = record.clinical.tumor.doids

        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined(
                "Could not determine whether patient has hormone-positive HER2-negative breast cancer",
                "Undetermined HR+ HER2- breast cancer type"
            )
        }
        val priorMolecularTests = record.clinical.priorMolecularTests
        val expandedDoidSet = DoidEvaluationFunctions.createFullExpandedDoidTree(doidModel, tumorDoids)
        val isBreastCancer = DoidConstants.BREAST_CANCER_DOID in expandedDoidSet
        val isProgesteronePositive = DoidConstants.PROGESTERONE_POSITIVE_BREAST_CANCER_DOID in expandedDoidSet
                || priorMolecularTests.any { it.item == "PR" && it.scoreText == "Positive" }
        val isProgesteroneNegative = DoidConstants.HER2_NEGATIVE_BREAST_CANCER_DOID in expandedDoidSet
                || priorMolecularTests.any { it.item == "PR" && it.scoreText == "Negative" }
        val isEstrogenPositive = DoidConstants.ESTROGEN_POSITIVE_BREAST_CANCER_DOID in expandedDoidSet
                || priorMolecularTests.any { it.item == "ER" && it.scoreText == "Positive" }
        val isEstrogenNegative = DoidConstants.HER2_NEGATIVE_BREAST_CANCER_DOID in expandedDoidSet
                || priorMolecularTests.any { it.item == "ER" && it.scoreText == "Negative" }
        val isHer2Positive = DoidConstants.HER2_POSITIVE_BREAST_CANCER_DOID in expandedDoidSet
                || priorMolecularTests.any { it.item == "HER2" && it.scoreText == "Positive" }
        val isHer2Negative = DoidConstants.HER2_NEGATIVE_BREAST_CANCER_DOID in expandedDoidSet
                || priorMolecularTests.any { it.item == "HER2" && it.scoreText == "Negative" }
        val hasERBB2Amplified = geneIsAmplifiedForPatient("ERBB2", record)

        return when {
            // Certainly Her2 negative + hormone positive (based on doids and/or IHC)
            isBreastCancer && isHer2Negative && (isProgesteronePositive || isEstrogenPositive) -> {
                if (hasERBB2Amplified) {
                    EvaluationFactory.warn(
                        "Patient has HER2-negative hormone-positive breast cancer but with ERBB2 amplified",
                        "Undetermined HR+ HER2- breast cancer due to presence of ERBB2 gene amp"
                    )
                } else {
                    EvaluationFactory.pass(
                        "Patient has hormone-positive and HER2 negative breast cancer",
                        "HR+ HER2- breast cancer"
                    )
                }
            }

            // Certainly her2 negative with unclear hormone status (no HR- doid/IHC but also no supporting data for HR+)
            isBreastCancer && isHer2Negative && (!isProgesteroneNegative || !isEstrogenNegative) -> {
                EvaluationFactory.undetermined(
                    "Patient has HER2-negative breast cancer but with unclear hormone status",
                    "Undetermined HR+ HER2- breast cancer due to HR status"
                )
            }

            // Certainly hormone positive with non-certain her2 negativity (no HER2+ doid/IHC but also no supporting data for HER2-)
            isBreastCancer && !isHer2Positive && (isProgesteronePositive || isEstrogenPositive) -> {
                EvaluationFactory.undetermined(
                    "Patient has hormone-positive breast cancer but with unclear HER2 status",
                    "Undetermined HR+ HER2- breast cancer due to HER2 status"
                )
            }

            // Certainly Her2-positive and/or hormone-negative
            isBreastCancer && (isHer2Positive || isEstrogenNegative || isProgesteroneNegative) -> {
                EvaluationFactory.fail(
                    "Patient does not have HR-positive HER2-negative breast cancer",
                    "No HR+/HER2- breast cancer"
                )
            }

            // Unclear HR/HER2 status based on doids and no IHC data available
            isBreastCancer -> {
                EvaluationFactory.undetermined(
                    "Breast cancer with undetermined HR/Her2-status since IHC data missing",
                    "Undetermined HR/Her2-status since IHC data missing"
                )
            }

            else -> EvaluationFactory.fail("Patient has no hormone-positive HER2-negative breast cancer", "Tumor type")
        }
    }
}