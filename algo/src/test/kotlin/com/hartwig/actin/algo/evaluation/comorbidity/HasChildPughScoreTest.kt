package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.comorbidity.ComorbidityTestFactory.otherCondition
import com.hartwig.actin.algo.evaluation.comorbidity.ComorbidityTestFactory.withOtherConditions
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasChildPughScoreTest {
    private val function = HasChildPughScore(TestIcdFactory.createTestModel(), listOf("A", "B"))

    @Test
    fun `Should be undetermined when liver cirrhosis not present`() {
        val conditions = listOf(
            otherCondition(icdMainCode = IcdConstants.HYPOMAGNESEMIA_CODE),
            otherCondition(icdMainCode = IcdConstants.PNEUMOTHORAX_CODE)
        )
        val result = function.evaluate(withOtherConditions(conditions))

        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.undeterminedMessagesStrings()).containsExactly("Undetermined if Child-Pugh score A or B")
    }

    @Test
    fun `Should warn when liver cirrhosis present`() {
        val conditions = listOf(
            otherCondition(icdMainCode = IcdConstants.LUNG_INFECTIONS_BLOCK),
            otherCondition(icdMainCode = IcdConstants.LIVER_CIRRHOSIS_CODE)
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.WARN, function.evaluate(withOtherConditions(conditions)))
    }
}