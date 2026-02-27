package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.datamodel.IcdNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasLeptomeningealDiseaseTest {

    private val targetCode = IcdConstants.LEPTOMENINGEAL_METASTASES_CODE
    private val targetNode = IcdNode(targetCode, emptyList(), "Leptomeningeal metastasis")
    private val childOfTargetNode = IcdNode("childCode", listOf(targetCode), "Child leptomeningeal metastasis")
    private val icdModel = IcdModel.create(listOf(targetNode, childOfTargetNode))
    private val function = HasLeptomeningealDisease(icdModel)

    @Test
    fun `Should pass when record contains other condition with direct or parent match on target icd code`() {
        listOf(targetNode.code, childOfTargetNode.code).forEach { code ->
            val condition = ComorbidityTestFactory.otherCondition(icdMainCode = code)
            assertEvaluation(EvaluationResult.PASS, function.evaluate(ComorbidityTestFactory.withOtherCondition(condition)))
        }
    }

    @Test
    fun `Should fail when no relevant conditions are present`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withOtherConditions(emptyList())))
    }

    @Test
    fun `Should fail when other conditions do not match leptomeningeal disease icd code`() {
        val different = ComorbidityTestFactory.otherCondition(icdMainCode = "other")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withOtherCondition(different)))
    }

    @Test
    fun `Should fail when CNS lesion is present but no leptomeningeal conditions`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withCnsLesion("just a lesion")))
    }

    @Test
    fun `Should warn when CNS lesion suggests leptomeningeal disease`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(ComorbidityTestFactory.withCnsLesion("carcinomatous meningitis"))
        )
    }

    @Test
    fun `Should warn when suspected CNS lesion suggests leptomeningeal disease`() {
        val evaluation = function.evaluate(ComorbidityTestFactory.withSuspectedCnsLesion("carcinomatous meningitis"))
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessagesStrings())
            .containsExactly("Has suspected lesions 'carcinomatous meningitis' potentially indicating leptomeningeal disease")
    }

    @Test
    fun `Should fail when suspected CNS lesion present but no suggestion of leptomeningeal disease`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withSuspectedCnsLesion("suspected CNS lesion")))
    }
}
