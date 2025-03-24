package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.datamodel.IcdNode
import org.junit.Test

class HasIntoleranceRelatedToStudyMedicationTest {

    private val icdModel = IcdModel.create(
        listOf(
            IcdNode(
                IcdConstants.NIVOLUMAB_CODE,
                listOf(IcdConstants.ANTI_NEOPLASTIC_AGENTS, IcdConstants.DRUG_ALLERGY_CODE),
                "Nivolumab"
            )
        )
    )
    private val function = HasIntoleranceRelatedToStudyMedication(icdModel)
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
    fun `Should fail when toxicity with matching ICD code has grade below 2 or null`() {
        val icdMainCode = matchingIcdCodes.first()
        listOf(
            ComorbidityTestFactory.toxicity("tox", ToxicitySource.EHR, 1, icdMainCode),
            ComorbidityTestFactory.toxicity("tox", ToxicitySource.EHR, null, icdMainCode)
        ).forEach { match ->
            assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withComorbidity(match)))
        }
    }

    @Test
    fun `Should fail when allergy is not to anti-cancer agent`() {
        val intolerance = ComorbidityTestFactory.intolerance(
            icdMainCode = matchingIcdCodes.first(),
            icdExtensionCode = IcdConstants.CEFAMYCIN_ANTIBIOTIC,
            clinicalStatus = HasIntoleranceRelatedToStudyMedication.CLINICAL_STATUS_ACTIVE
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withComorbidity(intolerance)))
    }

    @Test
    fun `Should evaluate to undetermined when active intolerance has matching ICD code`() {
        val intolerance = ComorbidityTestFactory.intolerance(
            icdMainCode = matchingIcdCodes.first(),
            icdExtensionCode = IcdConstants.NIVOLUMAB_CODE,
            clinicalStatus = HasIntoleranceRelatedToStudyMedication.CLINICAL_STATUS_ACTIVE
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ComorbidityTestFactory.withComorbidity(intolerance)))
    }

    @Test
    fun `Should evaluate to undetermined when any other comorbidity has matching ICD code`() {
        val icdMainCode = matchingIcdCodes.first()
        listOf(
            ComorbidityTestFactory.toxicity("tox", ToxicitySource.EHR, 2, icdMainCode, IcdConstants.NIVOLUMAB_CODE),
            ComorbidityTestFactory.otherCondition("condition", icdMainCode = icdMainCode, icdExtensionCode = IcdConstants.NIVOLUMAB_CODE),
            ComorbidityTestFactory.complication("complication", icdMainCode = icdMainCode, icdExtensionCode = IcdConstants.NIVOLUMAB_CODE)
        ).forEach { match ->
            assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ComorbidityTestFactory.withComorbidity(match)))
        }
    }
}