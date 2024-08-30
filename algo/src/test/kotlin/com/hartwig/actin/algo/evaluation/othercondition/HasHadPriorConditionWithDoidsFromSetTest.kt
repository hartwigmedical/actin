package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasHadPriorConditionWithDoidsFromSetTest {
    private val doidsToFind = DoidConstants.THROMBOEMBOLIC_EVENT_DOID_SET
    private val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    private val function = HasHadPriorConditionWithDoidsFromSet(doidModel, doidsToFind, "thrombo-embolic event")

    @Test
    fun `Should pass if condition with correct DOID term in history`() {
        val conditions = OtherConditionTestFactory.priorOtherCondition("deep vein thrombosis", doids = setOf(doidsToFind.first()))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(conditions)))
    }

    @Test
    fun `Should fail if no conditions with correct DOID term in history`() {
        val conditions = OtherConditionTestFactory.priorOtherCondition("lung disease", doids = setOf(DoidConstants.LUNG_DISEASE_DOID))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(conditions)))
    }

    @Test
    fun `Should fail if no conditions present in history`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(emptyList())))
    }
}