package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

private val PLATINUM_DRUG_SET = setOf("carboplatin", "cisplatin", "oxaliplatin")

class HasDrugIntoleranceWithAnyIcdCodeOrNameTest {

    private val function = HasDrugIntoleranceWithAnyIcdCodeOrName(
        TestIcdFactory.createTestModel(),
        IcdConstants.PLATINUM_COMPOUND_CODE,
        PLATINUM_DRUG_SET,
        "platinum compounds"
    )

    @Test
    fun `Should fail when no known intolerances are present`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withIntolerances(emptyList()))
        )
    }

    @Test
    fun `Should fail when intolerances does not have name or ICD code match for taxane intolerance`() {
        val mismatch = ComorbidityTestFactory.intolerance(name = "mismatch", icdMainCode = IcdConstants.PNEUMOTHORAX_CODE)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withComorbidity(mismatch))
        )
    }

    @Test
    fun `Should pass for intolerance matching on name only`() {
        val match = ComorbidityTestFactory.intolerance(name = PLATINUM_DRUG_SET.iterator().next())
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, function.evaluate(ComorbidityTestFactory.withComorbidity(match))
        )
    }

    @Test
    fun `Should pass for intolerance matching on ICD code only`() {
        val match = ComorbidityTestFactory.intolerance(
            icdMainCode = IcdConstants.DRUG_ALLERGY_SET.first(), icdExtensionCode = IcdConstants.PLATINUM_COMPOUND_CODE
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, function.evaluate(ComorbidityTestFactory.withComorbidity(match))
        )
    }

    @Test
    fun `Should pass for other comorbidity matching on ICD code only`() {
        val icdMainCode = IcdConstants.DRUG_ALLERGY_SET.first()
        val icdExtensionCode = IcdConstants.PLATINUM_COMPOUND_CODE
        listOf(
            ComorbidityTestFactory.toxicity("tox", ToxicitySource.EHR, 2, icdMainCode, icdExtensionCode),
            ComorbidityTestFactory.otherCondition("condition", icdMainCode = icdMainCode, icdExtensionCode = icdExtensionCode),
            ComorbidityTestFactory.complication("complication", icdMainCode = icdMainCode, icdExtensionCode = icdExtensionCode)
        ).forEach { match ->
            EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(ComorbidityTestFactory.withComorbidity(match)))
        }
    }

    @Test
    fun `Should pass when substring of intolerance name matches`() {
        val match = ComorbidityTestFactory.intolerance(name = "${PLATINUM_DRUG_SET.iterator().next()} chemotherapy allergy")

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, function.evaluate(ComorbidityTestFactory.withComorbidity(match))
        )
    }

    @Test
    fun `Should evaluate to undetermined when intolerance matches on drug allergy ICD code but has no ICD code extension`() {
        val intolerance = ComorbidityTestFactory.intolerance(icdMainCode = IcdConstants.DRUG_ALLERGY_SET.first(), icdExtensionCode = null)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(ComorbidityTestFactory.withComorbidity(intolerance))
        )
    }
}