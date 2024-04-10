package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasHistoryOfSecondMalignancyIgnoringDoidTermsTest{

    private val doidModel = TestDoidModelFactory.createWithOneParentChild("100", "200")
    private val function = HasHistoryOfSecondMalignancyIgnoringDoidTerms(doidModel, listOf("100"), listOf("ignore"))

    @Test
    fun `Should fail when no prior tumors present`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(emptyList())))
    }

    @Test
    fun `Should fail when prior tumors present in history but with doid to ignore`(){
        val priorTumors = listOf(PriorTumorTestFactory.priorSecondPrimary(doid = "100"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
    }

    @Test
    fun `Should pass when prior tumors present in history with doid term not to ignore`(){
        val priorTumors = listOf(PriorTumorTestFactory.priorSecondPrimary(doid = "200"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
    }
}
