package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasChildPughClassTest {
    val function = createTestChildPughFunction()

    @Test
    fun `Should not evaluate when liver cirrhosis not present`() {
        val conditions: List<PriorOtherCondition> = listOf(
            OtherConditionTestFactory.builder().addDoids(DoidConstants.HEART_DISEASE_DOID).build(),
            OtherConditionTestFactory.builder().addDoids(DoidConstants.LUNG_DISEASE_DOID).build()
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.NOT_EVALUATED,
            function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions))
        )
    }

    @Test
    fun `Should evaluate undetermined when liver cirrhosis present`() {
        val conditions: List<PriorOtherCondition> = listOf(
            OtherConditionTestFactory.builder().addDoids(DoidConstants.LUNG_DISEASE_DOID).build(),
            OtherConditionTestFactory.builder().addDoids(ALCOHOLIC_LIVER_CIRRHOSIS).build()
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions))
        )
    }

    companion object {
        private const val ALCOHOLIC_LIVER_CIRRHOSIS = "14018"

        private fun createTestChildPughFunction(): HasChildPughClass {
            return HasChildPughClass(
                TestDoidModelFactory.createWithOneParentChild(
                    DoidConstants.LIVER_CIRRHOSIS_DOID,
                    ALCOHOLIC_LIVER_CIRRHOSIS
                )
            )
        }
    }


}