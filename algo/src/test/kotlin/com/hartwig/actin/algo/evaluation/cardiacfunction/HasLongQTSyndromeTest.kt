package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasLongQTSyndromeTest {
    private val function = HasLongQTSyndrome(TestDoidModelFactory.createMinimalTestDoidModel())

    @Test
    fun `Should fail with no conditions`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withPriorOtherConditions(emptyList())))
    }
    
    @Test
    fun `Should fail with other condition`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withPriorOtherConditionsWithDoid("wrong doid")))
    }

    @Test
    fun `Should pass with matching condition`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withPriorOtherConditionsWithDoid(DoidConstants.LONG_QT_SYNDROME_DOID)))
    }

    companion object {
        private fun conditionWithDoid(doid: String): PriorOtherCondition {
            return PriorOtherCondition(name = "", category = "", isContraindicationForTherapy = true, doids = setOf(doid))
        }

        private fun withPriorOtherConditionsWithDoid(doid: String): PatientRecord {
            return withPriorOtherConditions(listOf(conditionWithDoid(doid)))
        }

        private fun withPriorOtherConditions(priorOtherConditions: List<PriorOtherCondition>): PatientRecord {
            return TestDataFactory.createMinimalTestPatientRecord().copy(
                clinical = TestClinicalFactory.createMinimalTestClinicalRecord().copy(
                    priorOtherConditions = priorOtherConditions
                )
            )
        }
    }
}