package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IcdCode
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
    fun `Should pass when record contains complication or non oncological history entry with direct or parent match on target icd code`() {
        listOf(targetNode.code, childOfTargetNode.code).flatMap { code ->
            listOf(
                ComplicationTestFactory.withComplication(
                    ComplicationTestFactory.complication(icdCode = IcdCode(code))
                ),
                OtherConditionTestFactory.withOtherCondition(
                    OtherConditionTestFactory.otherCondition(icdMainCode = code)
                )
            )
        }.forEach { assertEvaluation(EvaluationResult.PASS, function.evaluate(it)) }
    }

    @Test
    fun `Should fail when no complications are present`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(emptyList())))
    }

    @Test
    fun `Should fail when complications do not match leptomeningeal disease icd code`() {
        val different = ComplicationTestFactory.complication(icdCode = IcdCode("other"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplication(different)))
    }

    @Test
    fun `Should fail when CNS lesion is present but no leptomeningeal complications`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withCnsLesion("just a lesion")))
    }

    @Test
    fun `Should warn when CNS lesion suggests leptomeningeal disease`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(ComplicationTestFactory.withCnsLesion("carcinomatous meningitis"))
        )
    }

    @Test
    fun `Should warn when suspected CNS lesion suggests leptomeningeal disease`() {
        val evaluation = function.evaluate(ComplicationTestFactory.withSuspectedCnsLesion("carcinomatous meningitis"))
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessages)
            .containsExactly("Has suspected lesions 'carcinomatous meningitis' potentially indicating leptomeningeal disease")
    }

    @Test
    fun `Should fail when suspected CNS lesion present but no suggestion of leptomeningeal disease`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withSuspectedCnsLesion("suspected CNS lesion")))
    }
}
