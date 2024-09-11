package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

private const val NASAL_CAVITY_CANCER = "10911"

class HasLesionsCloseToOrInvolvingAirwayTest {

    private val function = HasLesionsCloseToOrInvolvingAirway(
        TestDoidModelFactory.createWithOneParentChild(
            DoidConstants.RESPIRATORY_SYSTEM_CANCER,
            NASAL_CAVITY_CANCER
        )
    )

    @Test
    fun `Should be undetermined if patient has no lung metastases or respiratory system cancer`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.LIVER_CANCER_DOID))
        )
    }

    @Test
    fun `Should pass if patient has lung metastases`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withLungLesions(true))
        )
    }

    @Test
    fun `Should pass if patient has nasal cavity cancer`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withDoids(NASAL_CAVITY_CANCER))
        )
    }
}