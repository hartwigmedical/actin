package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.priorOtherCondition
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.withPriorOtherConditions
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

class HasChildPughClassTest {
   private val function = HasChildPughClass(TestIcdFactory.createTestModel())

    @Test
    fun `Should not evaluate when liver cirrhosis not present`() {
        val conditions = listOf(
            priorOtherCondition(icdCode = IcdConstants.HYPOMAGNESEMIA_CODE),
            priorOtherCondition(icdCode = IcdConstants.PNEUMOTHORAX_CODE)
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(withPriorOtherConditions(conditions)))
    }

    @Test
    fun `Should evaluate undetermined when liver cirrhosis present`() {
        val conditions = listOf(
            priorOtherCondition(icdCode = IcdConstants.LUNG_INFECTIONS_BLOCK),
            priorOtherCondition(icdCode = IcdConstants.LIVER_CIRRHOSIS_CODE)
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withPriorOtherConditions(conditions)))
    }
}