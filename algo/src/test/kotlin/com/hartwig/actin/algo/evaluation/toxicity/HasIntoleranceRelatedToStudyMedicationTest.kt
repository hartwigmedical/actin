package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

class HasIntoleranceRelatedToStudyMedicationTest {

    private val function = HasIntoleranceRelatedToStudyMedication(TestIcdFactory.createTestModel())
    private val matchingIcdCodes = IcdConstants.DRUG_ALLERGY_SET

    @Test
    fun `Should fail when no comorbidities in history`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withIntolerances(emptyList())))
    }

    @Test
    fun `Should fail when intolerance has wrong ICD code`() {
        val intolerance = ComorbidityTestFactory.intolerance(icdMainCode = "wrong")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withComorbidity(intolerance)))
    }

    @Test
    fun `Should fail when intolerance with matching ICD code is not active`() {
        val intolerance = ComorbidityTestFactory.intolerance(icdMainCode = matchingIcdCodes.first(), clinicalStatus = "other")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withComorbidity(intolerance)))
    }

    @Test
    fun `Should evaluate to undetermined when active intolerance has matching ICD code`() {
        val intolerance = ComorbidityTestFactory.intolerance(
            icdMainCode = matchingIcdCodes.first(),
            clinicalStatus = HasIntoleranceRelatedToStudyMedication.CLINICAL_STATUS_ACTIVE
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ComorbidityTestFactory.withComorbidity(intolerance)))
    }

    @Test
    fun `Should evaluate to undetermined when any other comorbidity has matching ICD code`() {
        val icdMainCode = matchingIcdCodes.first()
        listOf(
            ComorbidityTestFactory.toxicity("tox", ToxicitySource.EHR, 2, icdMainCode),
            ComorbidityTestFactory.otherCondition("condition", icdMainCode = icdMainCode),
            ComorbidityTestFactory.complication("complication", icdMainCode = icdMainCode)
        ).forEach { match ->
            assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ComorbidityTestFactory.withComorbidity(match)))
        }
    }
}