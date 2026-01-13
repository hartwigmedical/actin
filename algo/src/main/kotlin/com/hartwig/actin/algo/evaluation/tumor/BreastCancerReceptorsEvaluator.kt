package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.molecular.IhcTestClassificationFunctions.TestResult
import com.hartwig.actin.algo.evaluation.molecular.IhcTestClassificationFunctions.classifyHer2Test
import com.hartwig.actin.algo.evaluation.molecular.IhcTestClassificationFunctions.classifyPrOrErTest
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.ReceptorType
import com.hartwig.actin.doid.DoidModel

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

enum class BreastCancerReceptorEvaluation {
    NOT_BREAST_CANCER,
    POSITIVE,
    BORDERLINE,
    LOW,
    NEGATIVE,
    DATA_MISSING,
    INCONSISTENT_DATA
}

class BreastCancerReceptorsEvaluator(private val doidModel: DoidModel) {

    fun evaluate(tumorDoids: Set<String>, ihcTests: List<IhcTest>, receptorType: ReceptorType): BreastCancerReceptorEvaluation {
        val targetIhcTests = ihcTests.filter { it.item == receptorType.display() }
        val testSummary = summarizeTests(targetIhcTests, receptorType)
        val positiveArguments = positiveArguments(testSummary, tumorDoids, receptorType)
        val lowArguments = TestResult.LOW in testSummary
        val negativeArguments = negativeArguments(testSummary, tumorDoids, receptorType)
        val specificArgumentsForStatusDeterminationMissing = !(positiveArguments || negativeArguments || lowArguments)

        return when {
            !isBreastCancer(tumorDoids) -> BreastCancerReceptorEvaluation.NOT_BREAST_CANCER
            targetIhcTests.isEmpty() && specificArgumentsForStatusDeterminationMissing -> BreastCancerReceptorEvaluation.DATA_MISSING
            listOf(positiveArguments, negativeArguments, lowArguments).count { it } > 1 -> BreastCancerReceptorEvaluation.INCONSISTENT_DATA
            positiveArguments -> BreastCancerReceptorEvaluation.POSITIVE
            lowArguments -> BreastCancerReceptorEvaluation.LOW
            TestResult.BORDERLINE in testSummary && !positiveArguments -> BreastCancerReceptorEvaluation.BORDERLINE
            else -> BreastCancerReceptorEvaluation.NEGATIVE
        }
    }

    private fun isBreastCancer(tumorDoids: Set<String>) =
        DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.BREAST_CANCER_DOID)

    private fun summarizeTests(targetIhcTests: List<IhcTest>, receptorType: ReceptorType): Set<TestResult> {
        val classifier = when (receptorType) {
            ReceptorType.ER, ReceptorType.PR -> ::classifyPrOrErTest
            ReceptorType.HER2 -> ::classifyHer2Test
        }
        return targetIhcTests.map(classifier).toSet()
    }

    private fun positiveArguments(testSummary: Set<TestResult>, tumorDoids: Set<String>, receptor: ReceptorType): Boolean {
        val targetReceptorPositiveInDoids = receptorPositiveInDoids(tumorDoids, receptor)
        return TestResult.POSITIVE in testSummary || targetReceptorPositiveInDoids
    }

    private fun negativeArguments(testSummary: Set<TestResult>, tumorDoids: Set<String>, receptor: ReceptorType): Boolean {
        val targetReceptorNegativeInDoids = receptorNegativeInDoids(tumorDoids, receptor)
        return TestResult.NEGATIVE in testSummary || targetReceptorNegativeInDoids
    }

    private fun receptorPositiveInDoids(tumorDoids: Set<String>, receptor: ReceptorType): Boolean {
        return DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, POSITIVE_DOID_MOLECULAR_COMBINATION[receptor]!!)
    }

    private fun receptorNegativeInDoids(tumorDoids: Set<String>, receptor: ReceptorType): Boolean {
        return DoidEvaluationFunctions.isOfAtLeastOneDoidType(
            doidModel,
            tumorDoids,
            setOfNotNull(NEGATIVE_DOID_MOLECULAR_COMBINATION[receptor], DoidConstants.TRIPLE_NEGATIVE_BREAST_CANCER_DOID)
        )
    }
}