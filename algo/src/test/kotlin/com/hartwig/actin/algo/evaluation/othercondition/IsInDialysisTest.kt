package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

class IsInDialysisTest {

    private val function = IsInDialysis(TestIcdFactory.createTestModel())

    @Test
    fun `Should pass if patient has comorbidity entry with dialysis ICD code`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(ComorbidityTestFactory.withComorbidity(
                ComorbidityTestFactory.otherCondition(icdMainCode = IcdConstants.DEPENDANCE_ON_RENAL_DIALYSIS_CODE))
            )
        )
    }

    @Test
    fun `Should fail if patient has comorbidity entries but not with dialysis ICD code`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(ComorbidityTestFactory.withComorbidity(
                ComorbidityTestFactory.otherCondition(icdMainCode = IcdConstants.HYPERTENSIVE_DISEASES_BLOCK))
            )
        )
    }

    @Test
    fun `Should fail for no comorbidities`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }
}