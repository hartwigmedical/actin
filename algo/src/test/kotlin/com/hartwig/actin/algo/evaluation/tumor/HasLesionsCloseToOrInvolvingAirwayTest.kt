package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasLesionsCloseToOrInvolvingAirwayTest {

    private val function = HasLesionsCloseToOrInvolvingAirway(
        TestDoidModelFactory.createWithOneDoidAndTerm(DoidConstants.TRACHEAL_CANCER, "tracheal cancer")
    )

    @Test
    fun `Should fail if patient has no respiratory system cancer and only liver lesions`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TumorTestFactory.withDoidsAndLiverLesions(setOf(DoidConstants.LIVER_CANCER_DOID), true)
                    .copy(tumor = TumorDetails().copy(hasLungLesions = false, otherLesions = emptyList()))
            )
        )
    }

    @Test
    fun `Should be undetermined if patient has no lung metastases or respiratory system cancer`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withDoids(DoidConstants.LIVER_CANCER_DOID)
                    .copy(tumor = TumorDetails().copy(otherLesions = listOf("Peritoneal")))
            )
        )
    }

    @Test
    fun `Should pass if patient has tracheal cancer`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.TRACHEAL_CANCER))
        )
    }

    @Test
    fun `Should warn if patient has lung cancer or lung lesions`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.LUNG_CANCER_DOID))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withLungLesions(true))
        )
    }
}