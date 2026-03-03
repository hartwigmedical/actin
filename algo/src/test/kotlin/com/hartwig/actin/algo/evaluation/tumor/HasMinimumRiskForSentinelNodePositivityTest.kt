package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasMinimumRiskForSentinelNodePositivityTest {

    @Test
    fun `Should evaluate to undetermined`() {
        val result = HasMinimumRiskForSentinelNodePositivity(40).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.undeterminedMessagesStrings()).containsExactly("Undetermined if patient has risk of sentinel node positivity above 40%")
    }
}