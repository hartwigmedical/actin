package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

class HasIntoleranceWithSpecificIcdTitleTest {

    private val targetIcdTitle = "targetParentTitle&targetExtensionParentTitle"
    private val icdModel =
        TestIcdFactory.createModelWithSpecificNodes(listOf("target", "targetParent", "targetExtension", "targetExtensionParent"))
    private val targetIcdCode = icdModel.resolveCodeForTitle(targetIcdTitle)!!
    private val childCode = icdModel.resolveCodeForTitle("targetTitle&targetExtensionTitle")!!
    private val function = HasIntoleranceWithSpecificIcdTitle(icdModel, targetIcdTitle)

    @Test
    fun `Should fail for no comorbidities`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withIntolerances(emptyList())))
    }

    @Test
    fun `Should fail for comorbidity with non-matching ICD code`() {
        assertResultForIcdCodes(EvaluationResult.FAIL, "wrong")
    }

    @Test
    fun `Should evaluate to undetermined for comorbidity with unknown extension`() {
        assertResultForIcdCodes(EvaluationResult.UNDETERMINED, targetIcdCode.mainCode)
    }

    @Test
    fun `Should pass for comorbidity with directly matching ICD code`() {
        assertResultForIcdCodes(EvaluationResult.PASS, targetIcdCode.mainCode, targetIcdCode.extensionCode)
    }

    @Test
    fun `Should pass for comorbidity with ICD code child of target title`() {
        assertResultForIcdCodes(EvaluationResult.PASS, childCode.mainCode, childCode.extensionCode)
    }

    private fun assertResultForIcdCodes(expectedResult: EvaluationResult, icdMainCode: String, icdExtensionCode: String? = null) {
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