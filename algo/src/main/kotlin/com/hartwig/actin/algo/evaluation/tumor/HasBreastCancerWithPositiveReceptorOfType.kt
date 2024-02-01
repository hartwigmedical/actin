package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.ReceptorType
import com.hartwig.actin.doid.DoidModel

class HasBreastCancerWithPositiveReceptorOfType(private val doidModel: DoidModel, private val receptorType: ReceptorType) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.clinical.tumor.doids
        val expandedDoidSet = DoidEvaluationFunctions.createFullExpandedDoidTree(doidModel, tumorDoids)
        val isBreastCancer = DoidConstants.BREAST_CANCER_DOID in expandedDoidSet
        val targetPriorMolecularTest = record.clinical.priorMolecularTests.filter { it.item == receptorType.display() }
        val targetReceptorPositiveInDoids = expandedDoidSet.contains(POSITIVE_DOID_MOLECULAR_COMBINATION[receptorType])
        val targetReceptorNegativeInDoids = expandedDoidSet.contains(NEGATIVE_DOID_MOLECULAR_COMBINATION[receptorType])

        val positiveArguments = targetPriorMolecularTest.any { it.scoreText == "Positive" }
                || positiveBasedOnScoreValue(record, receptorType) || targetReceptorPositiveInDoids
        val negativeArguments = targetPriorMolecularTest.any { it.scoreText == "Negative" }
                || negativeBasedOnScoreValue(record, receptorType) || targetReceptorNegativeInDoids

        val targetReceptorIsPositive = if (positiveArguments && !negativeArguments) true else null
        val targetReceptorIsNegative = if (negativeArguments && !positiveArguments) true else null
        val specificArgumentsForStatusDeterminationMissing = !(positiveArguments || negativeArguments)

        return when {
            tumorDoids.isNullOrEmpty() -> {
                EvaluationFactory.undetermined(
                    "Undetermined if $receptorType positive breast cancer since no tumor doids configured", "No tumor doids configured"
                )
            }

            !isBreastCancer -> EvaluationFactory.fail("Patient does not have breast cancer", "Tumor type")

            targetPriorMolecularTest.isEmpty() && !targetReceptorPositiveInDoids && !targetReceptorNegativeInDoids -> {
                EvaluationFactory.undetermined("$receptorType status unknown - data missing", "$receptorType status unknown")
            }

            (targetReceptorIsPositive == null && targetReceptorIsNegative == null && !specificArgumentsForStatusDeterminationMissing) -> {
                EvaluationFactory.undetermined(
                    "$receptorType-status undetermined since DOID and IHC data inconsistent",
                    "Undetermined $receptorType-status - DOID and IHC data inconsistent"
                )
            }

            targetReceptorIsPositive == true -> {
                EvaluationFactory.pass("Patient has $receptorType-positive breast cancer", "Has $receptorType-positive breast cancer")
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
            ReceptorType.ER to DoidConstants.ESTROGEN_POSITIVE_BREAST_CANCER_DOID,
            ReceptorType.PR to DoidConstants.PROGESTERONE_POSITIVE_BREAST_CANCER_DOID,
            ReceptorType.HER2 to DoidConstants.HER2_POSITIVE_BREAST_CANCER_DOID
        )
        private val NEGATIVE_DOID_MOLECULAR_COMBINATION = mapOf(
            ReceptorType.ER to DoidConstants.ESTROGEN_NEGATIVE_BREAST_CANCER_DOID,
            ReceptorType.PR to DoidConstants.PROGESTERONE_NEGATIVE_BREAST_CANCER_DOID,
            ReceptorType.HER2 to DoidConstants.HER2_NEGATIVE_BREAST_CANCER_DOID
        )

        fun negativeBasedOnScoreValue(record: PatientRecord, receptorType: ReceptorType): Boolean {
            val targetTest = record.clinical.priorMolecularTests.filter { it.item == receptorType.display() }
            val negativeTestResults = setOf(Triple("PR", 0, "%"), Triple("ER", 0, "%"), Triple("HER2", 0, "+"))
            return targetTest.map { Triple(it.item, it.scoreValue?.toInt(), it.scoreValueUnit) }.any(negativeTestResults::contains)
        }

        fun positiveBasedOnScoreValue(record: PatientRecord, receptorType: ReceptorType): Boolean {
            val targetTest = record.clinical.priorMolecularTests.filter { it.item == receptorType.display() }
            val positiveTestResults = setOf(Triple("PR", 100, "%"), Triple("ER", 100, "%"), Triple("HER2", 3, "+"))
            return targetTest.map { Triple(it.item, it.scoreValue?.toInt(), it.scoreValueUnit) }.any(positiveTestResults::contains)
        }
    }
}