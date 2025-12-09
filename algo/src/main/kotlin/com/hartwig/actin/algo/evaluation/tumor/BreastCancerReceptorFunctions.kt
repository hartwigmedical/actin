package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.molecular.IhcTestClassificationFunctions.TestResult
import com.hartwig.actin.algo.evaluation.molecular.IhcTestClassificationFunctions.classifyHer2Test
import com.hartwig.actin.algo.evaluation.molecular.IhcTestClassificationFunctions.classifyPrOrErTest
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.ReceptorType

object BreastCancerReceptorFunctions {

    val POSITIVE_DOID_MOLECULAR_COMBINATION = mapOf(
        ReceptorType.ER to DoidConstants.ESTROGEN_POSITIVE_BREAST_CANCER_DOID,
        ReceptorType.PR to DoidConstants.PROGESTERONE_POSITIVE_BREAST_CANCER_DOID,
        ReceptorType.HER2 to DoidConstants.HER2_POSITIVE_BREAST_CANCER_DOID
    )

    val NEGATIVE_DOID_MOLECULAR_COMBINATION = mapOf(
        ReceptorType.ER to DoidConstants.ESTROGEN_NEGATIVE_BREAST_CANCER_DOID,
        ReceptorType.PR to DoidConstants.PROGESTERONE_NEGATIVE_BREAST_CANCER_DOID,
        ReceptorType.HER2 to DoidConstants.HER2_NEGATIVE_BREAST_CANCER_DOID
    )

    fun summarizeTests(targetIhcTests: List<IhcTest>, receptorType: ReceptorType): Set<TestResult> {
        val classifier = when (receptorType) {
            ReceptorType.ER, ReceptorType.PR -> ::classifyPrOrErTest
            ReceptorType.HER2 -> ::classifyHer2Test
        }
        return targetIhcTests.map(classifier).toSet()
    }
}