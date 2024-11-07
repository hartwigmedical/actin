package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Complication
import org.junit.Test

class HasLeptomeningealDiseaseTest {

    private val function = HasLeptomeningealDisease()

    @Test
    fun `Should fail when no complications are present`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(emptyList())))
    }

    @Test
    fun `Should fail when complications do not match leptomeningeal disease`() {
        val different: Complication = ComplicationTestFactory.complication(categories = setOf("other complication"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplication(different)))
    }

    @Test
    fun `Should pass when complications match leptomeningeal disease categories`() {
        val matching: Complication = ComplicationTestFactory.complication(categories = setOf("leptomeningeal disease type 1"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withComplication(matching)))
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
    fun `Should warn when suspectd CNS lesion suggests leptomeningeal disease`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(ComplicationTestFactory.withSuspectedCnsLesion("carcinomatous meningitis"))
        )
    }

    @Test
    fun `Should fail when suspected CNS lesion present but no suggestion of leptomeningeal disease`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withSuspectedCnsLesion("suspected CNS lesion")))
    }
}
