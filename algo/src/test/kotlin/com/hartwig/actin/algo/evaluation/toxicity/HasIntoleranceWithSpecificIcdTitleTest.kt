package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
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
    fun `Should fail for no intolerances`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerances(emptyList())))
    }

    @Test
    fun `Should fail for intolerance with non-matching ICD code`() {
        val intolerance = ToxicityTestFactory.intolerance(icdMainCode = "wrong")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerance(intolerance)))
    }

    @Test
    fun `Should evaluate to undetermined for intolerance with unknown extension`() {
        val intolerance = ToxicityTestFactory.intolerance(icdMainCode = targetIcdCode.mainCode, icdExtensionCode = null)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ToxicityTestFactory.withIntolerance(intolerance)))
    }

    @Test
    fun `Should pass for intolerance with directly matching ICD code`() {
        val intolerance = ToxicityTestFactory.intolerance(icdMainCode = targetIcdCode.mainCode, icdExtensionCode = targetIcdCode.extensionCode)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withIntolerance(intolerance)))
    }

    @Test
    fun `Should pass for intolerance with ICD code child of target title`() {
        val intolerance = ToxicityTestFactory.intolerance(icdMainCode = childCode.mainCode, icdExtensionCode = childCode.extensionCode)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withIntolerance(intolerance)))
    }
}