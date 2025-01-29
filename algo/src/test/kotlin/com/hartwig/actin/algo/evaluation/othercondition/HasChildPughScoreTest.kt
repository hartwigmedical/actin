package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory.otherCondition
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory.withOtherConditions
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

class HasChildPughScoreTest {
   private val function = HasChildPughScore(TestIcdFactory.createTestModel())

    @Test
    fun `Should not evaluate when liver cirrhosis not present`() {
        val conditions = listOf(
            otherCondition(icdMainCode = IcdConstants.HYPOMAGNESEMIA_CODE),
            otherCondition(icdMainCode = IcdConstants.PNEUMOTHORAX_CODE)
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(withOtherConditions(conditions)))
    }

    @Test
    fun `Should evaluate undetermined when liver cirrhosis present`() {
        val conditions = listOf(
            otherCondition(icdMainCode = IcdConstants.LUNG_INFECTIONS_BLOCK),
            otherCondition(icdMainCode = IcdConstants.LIVER_CIRRHOSIS_CODE)
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withOtherConditions(conditions)))
    }
}