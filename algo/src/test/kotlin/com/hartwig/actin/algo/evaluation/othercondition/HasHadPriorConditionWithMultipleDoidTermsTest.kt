package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasHadPriorConditionWithMultipleDoidTermsTest {

    val doidsToFind = DoidConstants.THROMBO_EMBOLIC_EVENT_DOID_SET
    val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    val function = HasHadPriorConditionWithMultipleDoidTerms(doidModel, doidsToFind, "thrombo-embolic event")


    @Test
    fun `Should pass if condition with correct DOID term in history`() {
        val conditions = OtherConditionTestFactory.priorOtherCondition("deep vein thrombosis", doids = setOf(doidsToFind.first()))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(conditions)))
    }

    @Test
    fun `Should fail if no conditions with correct DOID term in history`() {
        val conditions = OtherConditionTestFactory.priorOtherCondition(
            "lung disease",
            doids = setOf(DoidConstants.LUNG_DISEASE_DOID)
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(conditions)))
    }

}