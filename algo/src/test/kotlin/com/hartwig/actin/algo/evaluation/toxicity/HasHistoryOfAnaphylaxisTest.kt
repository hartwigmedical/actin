package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.datamodel.IcdNode
import org.junit.Test

class HasHistoryOfAnaphylaxisTest {

    private val icdModel = IcdModel.create(
        listOf(
            IcdNode(IcdConstants.ANAPHYLAXIS_CODE, emptyList(), "Anaphylaxis"),
            IcdNode(IcdConstants.DRUG_INDUCED_ANAPHYLAXIS_CODE, listOf(IcdConstants.ANAPHYLAXIS_CODE), "Drug-induced anaphylaxis")
        )
    )
    private val function = HasHistoryOfAnaphylaxis(icdModel)
    private val testPatient = TestPatientFactory.createMinimalTestWGSPatientRecord()

    @Test
    fun `Should pass for matching condition entry`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                testPatient.copy(
                    comorbidities = listOf(
                        ComorbidityTestFactory.otherCondition(icdMainCode = IcdConstants.ANAPHYLAXIS_CODE)
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass for matching intolerance entry`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                testPatient.copy(
                    comorbidities = listOf(
                        ComorbidityTestFactory.intolerance(icdMainCode = IcdConstants.DRUG_INDUCED_ANAPHYLAXIS_CODE)
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail for empty history and no intolerances`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(testPatient.copy(comorbidities = emptyList()))
        )
    }

    @Test
    fun `Should fail if only non-matching history and intolerance entries present`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                testPatient.copy(
                    comorbidities = listOf(
                        ComorbidityTestFactory.intolerance(icdMainCode = "wrong"),
                        ComorbidityTestFactory.otherCondition(icdMainCode = "wrong")
                    )
                )
            )
        )
    }
}