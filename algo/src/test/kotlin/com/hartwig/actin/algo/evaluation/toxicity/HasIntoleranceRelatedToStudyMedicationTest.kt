package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

class HasIntoleranceRelatedToStudyMedicationTest {

    private val function = HasIntoleranceRelatedToStudyMedication(TestIcdFactory.createTestModel())
    private val matchingIcdCodes = IcdConstants.DRUG_ALLERGY_SET

    @Test
    fun `Should fail when no intolerances in history`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerances(emptyList())))
    }

    @Test
    fun `Should fail when intolerance has wrong ICD code`() {
        val intolerance = ToxicityTestFactory.intolerance(icdMainCode = "wrong")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerance(intolerance)))
    }

    @Test
    fun `Should evaluate to undetermined when intolerance has matching ICD code`() {
        val intolerance = ToxicityTestFactory.intolerance(
            icdMainCode = matchingIcdCodes.first(),
            clinicalStatus = HasIntoleranceRelatedToStudyMedication.CLINICAL_STATUS_ACTIVE
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ToxicityTestFactory.withIntolerance(intolerance)))
    }
}