package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

private const val matchDoid = "match doid"
private const val matchTerm = "match term"
private const val parentDoid = "parent doid"
private const val parentTerm = "parent term"

class HasHistoryOfSecondMalignancyIgnoringDoidTermsTest {

    private val doidModel = TestDoidModelFactory.createWithParentChildAndTermPerDoidMaps(
        mapOf(matchDoid to parentDoid),
        mapOf(matchDoid to matchTerm, parentDoid to parentTerm)
    )
    private val function = HasHistoryOfSecondMalignancyIgnoringDoidTerms(doidModel, listOf(matchTerm))

    @Test
    fun `Should fail when no prior tumors present`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(emptyList())))
    }

    @Test
    fun `Should fail when prior tumors present in history but with doid to ignore`(){
        val priorTumors = listOf(PriorTumorTestFactory.priorSecondPrimary(doid = matchDoid))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
    }

    @Test
    fun `Should fail when prior tumors present in history but doid is child of doid to ignore`(){
        val priorTumors = listOf(PriorTumorTestFactory.priorSecondPrimary(doid = matchDoid))
        val function = HasHistoryOfSecondMalignancyIgnoringDoidTerms(doidModel, listOf(parentTerm))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
    }

    @Test
    fun `Should pass when prior tumors present in history with doid term not to ignore`(){
        val priorTumors = listOf(PriorTumorTestFactory.priorSecondPrimary(doid = "other"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
    }
}
