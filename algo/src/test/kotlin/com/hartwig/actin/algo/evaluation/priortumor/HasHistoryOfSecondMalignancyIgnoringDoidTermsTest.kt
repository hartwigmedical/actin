package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test
import java.time.LocalDate

private const val matchDoid = "match doid"
private const val matchTerm = "match term"
private const val parentDoid = "parent doid"
private const val parentTerm = "parent term"

class HasHistoryOfSecondMalignancyIgnoringDoidTermsTest {

    private val minDate = LocalDate.of(2024, 4, 22)
    private val doidModel = TestDoidModelFactory.createWithParentChildAndTermPerDoidMaps(
        mapOf(matchDoid to parentDoid),
        mapOf(matchDoid to matchTerm, parentDoid to parentTerm),
    )
    private val function = HasHistoryOfSecondMalignancyIgnoringDoidTerms(doidModel, listOf(matchTerm), minDate)

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
        val function = HasHistoryOfSecondMalignancyIgnoringDoidTerms(doidModel, listOf(parentTerm), minDate)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
    }

    @Test
    fun `Should pass when prior tumors present in history with doid term not to ignore`(){
        val priorTumors = listOf(PriorTumorTestFactory.priorSecondPrimary(doid = "other", diagnosedYear = minDate.year))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
    }

    @Test
    fun `Should evaluate to undetermined when prior tumors present in history with doid term not to ignore but date unknown`(){
        val priorTumors = listOf(PriorTumorTestFactory.priorSecondPrimary(doid = "other", diagnosedYear = null))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
    }

    @Test
    fun `Should fail when prior tumors with doid term to ignore and unknown date in history`(){
        val priorTumors = listOf(PriorTumorTestFactory.priorSecondPrimary(doid = matchDoid, diagnosedYear = null))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
    }

    @Test
    fun `Should fail when prior tumors present in history with doid term not to ignore but outside date range to evaluate`(){
        val priorTumors = listOf(PriorTumorTestFactory.priorSecondPrimary(doid = "other", diagnosedYear = minDate.minusYears(2).year))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
    }
}
