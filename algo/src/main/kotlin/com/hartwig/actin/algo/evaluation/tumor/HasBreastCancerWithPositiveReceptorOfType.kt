package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.doid.DoidModel

class HasBreastCancerWithPositiveReceptorOfType(private val doidModel: DoidModel, private val receptorType: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.clinical.tumor.doids
        val expandedDoidSet = DoidEvaluationFunctions.createFullExpandedDoidTree(doidModel, tumorDoids)
        val isBreastCancer = DoidConstants.BREAST_CANCER_DOID in expandedDoidSet
        val targetPriorMolecularTest = record.clinical.priorMolecularTests.filter { it.item == receptorType }
        val targetPriorMolecularTestIsPositive = targetPriorMolecularTest.any { it.scoreText == "Positive" }
        val targetPriorMolecularTestIsNegative = targetPriorMolecularTest.any { it.scoreText == "Negative" }
        val targetReceptorPositiveInDoids = expandedDoidSet.contains(POSITIVE_DOID_MOLECULAR_COMBINATION[receptorType])
        val targetReceptorNegativeInDoids = expandedDoidSet.contains(NEGATIVE_DOID_MOLECULAR_COMBINATION[receptorType])

        return when {
            !isBreastCancer -> EvaluationFactory.fail("Patient does not have breast cancer", "Tumor type")

            targetPriorMolecularTest.isEmpty() && !targetReceptorPositiveInDoids -> {
                EvaluationFactory.undetermined("$receptorType status unknown - data missing", "$receptorType status unknown")
            }

            (targetPriorMolecularTestIsNegative && targetReceptorPositiveInDoids) ||
                    (targetPriorMolecularTestIsPositive && targetReceptorNegativeInDoids) -> {
                EvaluationFactory.undetermined(
                    "$receptorType-status undetermined since DOID and IHC data inconsistent",
                    "Undetermined $receptorType-status - DOID and IHC data inconsistent"
                )
            }

            targetPriorMolecularTestIsPositive || targetReceptorPositiveInDoids -> {
                EvaluationFactory.pass("Patient has $receptorType-positive breast cancer", "$receptorType-positive breast cancer")
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient does not have $receptorType-positive breast cancer",
                    "No $receptorType-positive breast cancer"
                )
            }
        }
    }

    companion object {
        private val POSITIVE_DOID_MOLECULAR_COMBINATION = mapOf(
            "ER" to DoidConstants.ESTROGEN_POSITIVE_BREAST_CANCER_DOID,
            "PR" to DoidConstants.PROGESTERONE_POSITIVE_BREAST_CANCER_DOID,
            "HER2" to DoidConstants.HER2_POSITIVE_BREAST_CANCER_DOID
        )
        private val NEGATIVE_DOID_MOLECULAR_COMBINATION = mapOf(
            "ER" to DoidConstants.ESTROGEN_NEGATIVE_BREAST_CANCER_DOID,
            "PR" to DoidConstants.PROGESTERONE_NEGATIVE_BREAST_CANCER_DOID,
            "HER2" to DoidConstants.HER2_NEGATIVE_BREAST_CANCER_DOID
        )
    }

}