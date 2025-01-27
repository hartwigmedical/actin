package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

private val AUTOIMMUNE_ICD_MAIN_CODE = IcdConstants.AUTOIMMUNE_DISEASE_SET.first()
private val MATCHING_ICD_MAIN_CODE = IcdConstants.DRUG_ALLERGY_SET.first()
private val OTHER_MATCHING_ICD_MAIN_CODE = IcdConstants.DRUG_ALLERGY_SET.last()

class HasIntoleranceForPD1OrPDL1InhibitorsTest {
    private val function = HasIntoleranceForPD1OrPDL1Inhibitors(TestIcdFactory.createTestModel())

    @Test
    fun `Should pass when patient has intolerance matching name`() {
        HasIntoleranceForPD1OrPDL1Inhibitors.INTOLERANCE_TERMS.forEach { term: String ->
            val record = patient(listOf(ComorbidityTestFactory.intolerance(name = "intolerance to " + term.uppercase())))
            assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
        }
    }

    @Test
    fun `Should pass when patient has any comorbidity matching main and extension code`() {
        assertResultForIcdCodes(EvaluationResult.PASS, MATCHING_ICD_MAIN_CODE, IcdConstants.PD_L1_PD_1_DRUG_SET.first())
    }

    @Test
    fun `Should evaluate to undetermined if any comorbidity matches on ICD main code but extension code unknown`() {
        assertResultForIcdCodes(EvaluationResult.UNDETERMINED, MATCHING_ICD_MAIN_CODE, null)
    }

    @Test
    fun `Should evaluate to undetermined if any comorbidity matches on ICD main code and extension code monoclonal antibodies`() {
        assertResultForIcdCodes(EvaluationResult.UNDETERMINED, OTHER_MATCHING_ICD_MAIN_CODE, IcdConstants.MONOCLONAL_ANTIBODY_BLOCK)
    }

    @Test
    fun `Should warn when patient has prior condition belonging to autoimmune disease ICD code`() {
        assertEvaluation(EvaluationResult.WARN, function.evaluate(patient(emptyList(), AUTOIMMUNE_ICD_MAIN_CODE)))
    }

    @Test
    fun `Should fail when patient has no matching intolerance or autoimmune disease condition`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(patient(listOf(ComorbidityTestFactory.intolerance(name = "other")), "123"))
        )
    }

    @Test
    fun `Should fail for empty intolerance list`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withIntolerances(emptyList())))
    }

    private fun patient(intolerances: List<Intolerance>, icdMainCode: String = ""): PatientRecord {
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            comorbidities = intolerances + ComorbidityTestFactory.otherCondition(icdMainCode = icdMainCode)
        )
    }

    private fun assertResultForIcdCodes(expectedResult: EvaluationResult, icdMainCode: String, icdExtensionCode: String?) {
        listOf(
            ComorbidityTestFactory.intolerance("unspecified", icdMainCode, icdExtensionCode),
            ComorbidityTestFactory.toxicity("tox", ToxicitySource.EHR, 2, icdMainCode, icdExtensionCode),
            ComorbidityTestFactory.otherCondition("condition", icdMainCode = icdMainCode, icdExtensionCode = icdExtensionCode),
            ComorbidityTestFactory.complication("complication", icdMainCode = icdMainCode, icdExtensionCode = icdExtensionCode)
        ).forEach { match ->
            assertEvaluation(expectedResult, function.evaluate(ComorbidityTestFactory.withComorbidity(match)))
        }
    }
}