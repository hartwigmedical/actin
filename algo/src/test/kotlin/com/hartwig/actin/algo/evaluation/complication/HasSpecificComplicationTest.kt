package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasSpecificComplicationTest {

    private val icdModel = TestIcdFactory.createModelWithSpecificNodes(listOf("target", "targetParent", "otherTarget", "wrong"))
    private val function = HasSpecificComplication(icdModel, listOf("targetTitle", "otherTargetTitle"))
    private val targetComplication = ComplicationTestFactory.complication(name = "random name", icdCode = IcdCode("targetCode"))

    @Test
    fun `Should return undetermined when complications is null`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ComplicationTestFactory.withComplications(null)))
    }

    @Test
    fun `Should pass when icd code of complication matches the code of one of the target icd titles`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withComplications(listOf(targetComplication))))
    }

    @Test
    fun `Should pass when parent icd code of complication matches the code of one of the target icd titles`() {
        val function = HasSpecificComplication(icdModel, listOf("targetParentTitle"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withComplications(listOf(targetComplication))))
    }

    @Test
    fun `Should pass with correct message when icd code of complication matches the code of all of the target icd titles`() {
        val otherTarget = targetComplication.copy(name = "other", icdCode = IcdCode("otherTargetCode"))
        val evaluation = function.evaluate(ComplicationTestFactory.withComplications(listOf(targetComplication, otherTarget)))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passSpecificMessages).containsExactly("Patient has complication(s) other and random name")
    }

    @Test
    fun `Should fail when icd code of complication does not match the code of any of the target icd titles`() {
        val wrong = targetComplication.copy(icdCode = IcdCode("wrongCode"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(listOf(wrong))))
    }

    @Test
    fun `Should fail for no complications`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(emptyList())))
    }

    @Test
    fun `Should return undetermined when complications is not empty but none of the complications have a name (regardless icdCode)`() {
        val yesInput = ComplicationTestFactory.yesInputComplication()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ComplicationTestFactory.withComplications(listOf(yesInput))))
    }
}