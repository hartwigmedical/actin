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

class BreastCancerReceptorsEvaluator(private val doidModel: DoidModel) {

    fun isBreastCancer(tumorDoids: Set<String>) =
        DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.BREAST_CANCER_DOID)

    fun summarizeTests(targetIhcTests: List<IhcTest>, receptorType: ReceptorType): Set<TestResult> {
        val classifier = when (receptorType) {
            ReceptorType.ER, ReceptorType.PR -> ::classifyPrOrErTest
            ReceptorType.HER2 -> ::classifyHer2Test
        }
        return targetIhcTests.map(classifier).toSet()
    }

    fun receptorIsPositive(positiveArguments: Boolean, negativeArguments: Boolean): Boolean? {
        return when {
            positiveArguments && !negativeArguments -> true
            negativeArguments && !positiveArguments -> false
            else -> null
        }
    }

    fun positiveArguments(testSummary: Set<TestResult>, tumorDoids: Set<String>, receptor: ReceptorType): Boolean {
        val targetReceptorPositiveInDoids = receptorPositiveInDoids(tumorDoids, receptor)
        return TestResult.POSITIVE in testSummary || targetReceptorPositiveInDoids
    }

    fun negativeArguments(testSummary: Set<TestResult>, tumorDoids: Set<String>, receptor: ReceptorType): Boolean {
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