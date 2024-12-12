package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

class HasIntoleranceToPlatinumCompoundsTest {

    private val function = HasIntoleranceToPlatinumCompounds(TestIcdFactory.createTestModel())

    @Test
    fun `Should fail when no known intolerances are present`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerances(emptyList()))
        )
    }

    @Test
    fun `Should fail when intolerances does not have name or ICD code match for taxane intolerance`() {
        val mismatch = ToxicityTestFactory.intolerance(name = "mismatch", icdMainCode = IcdConstants.PNEUMOTHORAX_CODE)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerance(mismatch))
        )
    }

    @Test
    fun `Should pass for intolerance matching on name only`() {
        val match = ToxicityTestFactory.intolerance(name = HasIntoleranceToPlatinumCompounds.PLATINUM_COMPOUNDS.iterator().next())
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withIntolerance(match))
        )
    }

    @Test
    fun `Should pass for intolerance matching on ICD code only`() {
        val match = ToxicityTestFactory.intolerance(
            icdMainCode = IcdConstants.DRUG_ALLERGY_SET.first(), icdExtensionCode = IcdConstants.PLATINUM_COMPOUND_CODE
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withIntolerance(match))
        )
    }

    @Test
    fun `Should pass when substring of intolerance name matches`() {
        val match = ToxicityTestFactory.intolerance(name = "carboplatin chemotherapy allergy")

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withIntolerance(match))
        )
    }

    @Test
    fun `Should evaluate to undetermined when intolerance matches on drug allergy ICD code but has no ICD code extension`() {
        val intolerance = ToxicityTestFactory.intolerance(icdMainCode = IcdConstants.DRUG_ALLERGY_SET.first(), icdExtensionCode = null)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(ToxicityTestFactory.withIntolerance(intolerance))
        )
    }
}