package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator.geneIsAmplifiedForPatient
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
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
                || hasPositiveTest(targetPriorMolecularTest, receptorType) || targetReceptorPositiveInDoids
        val negativeArguments = targetPriorMolecularTest.any { it.scoreText == "Negative" }
                || negativeBasedOnScoreValue(targetPriorMolecularTest, receptorType) || targetReceptorNegativeInDoids

        val targetReceptorIsPositive = when {
            positiveArguments && !negativeArguments -> true
            negativeArguments && !positiveArguments -> false
            else -> null
        }
        val specificArgumentsForStatusDeterminationMissing = !(positiveArguments || negativeArguments)

//        Certainly Her2 negative + hormone positive (based on doids and/or IHC)
//        isBreastCancer && isHer2Negative && (isProgesteronePositive || isEstrogenPositive) -> {
//            if (hasERBB2Amplified) {
//        EvaluationFactory.warn(
//            "Patient has HER2-negative hormone-positive breast cancer but with ERBB2 amplified",
//            "Undetermined HR+ HER2- breast cancer due to presence of ERBB2 gene amp"
//        )

        return when {
            tumorDoids.isNullOrEmpty() -> {
                EvaluationFactory.undetermined(
                    "Undetermined if $receptorType positive breast cancer since no tumor doids configured", "No tumor doids configured"
                )
            }

            !isBreastCancer -> EvaluationFactory.fail("Patient does not have breast cancer", "Tumor type")

            targetPriorMolecularTest.isEmpty() && !targetReceptorPositiveInDoids && !targetReceptorNegativeInDoids -> {
                EvaluationFactory.undetermined(
                    "${receptorType.display()} status unknown - data missing",
                    "${receptorType.display()} status unknown"
                )
            }

            (targetReceptorIsPositive == null && !specificArgumentsForStatusDeterminationMissing) -> {
                EvaluationFactory.undetermined(
                    "${receptorType.display()}-status undetermined since DOID and IHC data inconsistent",
                    "Undetermined ${receptorType.display()}-status - DOID and IHC data inconsistent"
                )
            }

            targetReceptorIsPositive == true -> {
                EvaluationFactory.pass(
                    "Patient has ${receptorType.display()}-positive breast cancer",
                    "Has ${receptorType.display()}-positive breast cancer"
                )
            }

            receptorType == ReceptorType.HER2 && geneIsAmplifiedForPatient("ERBB2", record) -> {
                EvaluationFactory.warn(
                    "Patient has ${receptorType.display()}-positive breast cancer based on DOIDS and/or prior molecular tests " +
                            "but undetermined if true since ERBB2 gene amp present",
                    "Undetermined if ${receptorType.display()}-positive breast cancer since DOID/IHC data inconsistent with ERBB2 gene amp"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient does not have ${receptorType.display()}-positive breast cancer",
                    "No ${receptorType.display()}-positive breast cancer"
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

        fun negativeBasedOnScoreValue(targetPriorMolecularTest: List<PriorMolecularTest>, receptorType: ReceptorType): Boolean {
//            val targetTest = record.clinical.priorMolecularTests.filter { it.item == receptorType.display() }
//            val negativeTestResults = setOf(Triple("PR", 0, "%"), Triple("ER", 0, "%"), Triple("HER2", 0, "+"))
//            return targetTest.map { Triple(it.item, it.scoreValue?.toInt(), it.scoreValueUnit) }.any(negativeTestResults::contains)
            val (scoreValue, scoreValueUnit) = when (receptorType) {
                ReceptorType.PR, ReceptorType.ER -> {
                    Pair(0, "%")
                }

                ReceptorType.HER2 -> {
                    Pair(0, "+")
                }
            }
            return targetPriorMolecularTest.any {
                it.scoreText?.lowercase() == "negative" || it.scoreValue?.toInt() == scoreValue && it.scoreValueUnit == scoreValueUnit
            }
        }

        fun hasPositiveTest(targetPriorMolecularTest: List<PriorMolecularTest>, receptorType: ReceptorType): Boolean {
//            val targetTest = record.clinical.priorMolecularTests.filter { it.item == receptorType.display() }
//            val positiveTestResults = setOf(Triple("PR", 100, "%"), Triple("ER", 100, "%"), Triple("HER2", 3, "+"))
//            return targetTest.map { Triple(it.item, it.scoreValue?.toInt(), it.scoreValueUnit) }.any(positiveTestResults::contains)
            val (scoreValue, scoreValueUnit) = when (receptorType) {
                ReceptorType.PR, ReceptorType.ER -> {
                    Pair(100, "%")
                }

                ReceptorType.HER2 -> {
                    Pair(3, "+")
                }
            }
            return targetPriorMolecularTest.any {
                it.scoreText?.lowercase() == "positive" || it.scoreValue?.toInt() == scoreValue && it.scoreValueUnit == scoreValueUnit
            }
        }
    }
}