package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.TestPriorOtherConditionFactory
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
            val record = patient(listOf(ToxicityTestFactory.intolerance(name = "intolerance to " + term.uppercase())))
            assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
        }
    }

    @Test
    fun `Should pass when patient has intolerance matching main and extension code`() {
        evaluateWithCodes(
            EvaluationResult.PASS, listOf(
                IcdCode(MATCHING_ICD_MAIN_CODE, IcdConstants.PD_L1_PD_1_DRUG_SET.first()),
                IcdCode(OTHER_MATCHING_ICD_MAIN_CODE, IcdConstants.PD_L1_PD_1_DRUG_SET.last())
            )
        )
    }

    @Test
    fun `Should evaluate to undetermined if intolerance matches on ICD main code but extension code unknown`() {
        evaluateWithCodes(
            EvaluationResult.UNDETERMINED, listOf(
                IcdCode(MATCHING_ICD_MAIN_CODE, null),
            )
        )
    }

    @Test
    fun `Should evaluate to undetermined if intolerance matches on ICD main code and extension code monoclonal antibodies`() {
        evaluateWithCodes(
            EvaluationResult.UNDETERMINED, listOf(
                IcdCode(OTHER_MATCHING_ICD_MAIN_CODE, IcdConstants.MONOCLONAL_ANTIBODY_BLOCK)
            )
        )
    }

    @Test
    fun `Should warn when patient has prior condition belonging to autoimmune disease ICD code`() {
        assertEvaluation(EvaluationResult.WARN, function.evaluate(patient(emptyList(), AUTOIMMUNE_ICD_MAIN_CODE)))
    }

    @Test
    fun `Should fail when patient has matching condition but no contraindication for therapy`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                patient(
                    listOf(ToxicityTestFactory.intolerance(name = "other")),
                    AUTOIMMUNE_ICD_MAIN_CODE,
                    isContraIndication = false
                )
            )
        )
    }

    @Test
    fun `Should fail when patient has no matching intolerance or autoimmune disease condition`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(patient(listOf(ToxicityTestFactory.intolerance(name = "other")), "123"))
        )
    }

    @Test
    fun `Should fail for empty intolerance list`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withIntolerances(emptyList())))
    }

    private fun patient(intolerances: List<Intolerance>, icdMainCode: String = "", isContraIndication: Boolean = true): PatientRecord {
        val priorCondition = TestPriorOtherConditionFactory.createMinimal()
            .copy(icdCodes = setOf(IcdCode(icdMainCode)), isContraindicationForTherapy = isContraIndication)
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            comorbidities = intolerances + priorCondition
        )
    }

    private fun evaluateWithCodes(expected: EvaluationResult, codeList: List<IcdCode>) {
        codeList.forEach { code: IcdCode ->
            val record =
                patient(listOf(ToxicityTestFactory.intolerance(icdMainCode = code.mainCode, icdExtensionCode = code.extensionCode)))
            assertEvaluation(expected, function.evaluate(record))
        }
    }
}