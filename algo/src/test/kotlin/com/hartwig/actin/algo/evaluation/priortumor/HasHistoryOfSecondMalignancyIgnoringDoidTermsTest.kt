package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test
import java.time.LocalDate

private const val ignoreDoid = "ignore doid"
private const val ignoreTerm = "ignore term"
private const val parentDoid = "parent doid"
private const val parentTerm = "parent term"

class HasHistoryOfSecondMalignancyIgnoringDoidTermsTest {

    private val minDate = LocalDate.of(2024, 4, 22)
    private val doidModel = TestDoidModelFactory.createWithParentChildAndTermPerDoidMaps(
        mapOf(ignoreDoid to parentDoid),
        mapOf(ignoreDoid to ignoreTerm, parentDoid to parentTerm),
    )
    private val functionWithoutMinDate = HasHistoryOfSecondMalignancyIgnoringDoidTerms(doidModel, listOf(ignoreTerm), minDate = null)
    private val functionWithMinDate = HasHistoryOfSecondMalignancyIgnoringDoidTerms(doidModel, listOf(ignoreTerm), minDate = minDate)

    @Test
    fun `Should fail when no prior tumors present`() {
        assertEvaluation(EvaluationResult.FAIL, functionWithoutMinDate.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(emptyList())))
    }

    @Test
    fun `Should fail when prior tumors present in history but with doid to ignore`() {
        val priorTumors = listOf(PriorTumorTestFactory.priorSecondPrimary(doid = ignoreDoid))
        assertEvaluation(EvaluationResult.FAIL, functionWithoutMinDate.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
    }

    @Test
    fun `Should fail when prior tumors present in history but doid is child of doid to ignore`() {
        val priorTumors = listOf(PriorTumorTestFactory.priorSecondPrimary(doid = ignoreDoid))
        val function = HasHistoryOfSecondMalignancyIgnoringDoidTerms(doidModel, listOf(parentTerm), minDate = null)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
    }

    @Test
    fun `Should pass when prior tumors present in history with doid term not to ignore`() {
        val priorTumors = listOf(PriorTumorTestFactory.priorSecondPrimary(doid = "other", diagnosedYear = minDate.year))
        assertEvaluation(EvaluationResult.PASS, functionWithoutMinDate.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
    }

    @Test
    fun `Should pass when prior tumors present in history with doid term not to ignore and within requested date range`() {
        val priorTumors = listOf(PriorTumorTestFactory.priorSecondPrimary(doid = "other", diagnosedYear = minDate.year))
        assertEvaluation(EvaluationResult.PASS, functionWithMinDate.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
    }

    @Test
    fun `Should evaluate to undetermined when prior tumors present in history with doid term not to ignore but date unknown`(){
        val priorTumors = listOf(PriorTumorTestFactory.priorSecondPrimary(
            doid = "other", diagnosedYear = null, diagnosedMonth = null, lastTreatmentMonth = null, lastTreatmentYear = null)
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED, functionWithMinDate.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors))
        )
    }

    @Test
    fun `Should fail when prior tumors with doid term to ignore and unknown date in history`(){
        val priorTumors = listOf(PriorTumorTestFactory.priorSecondPrimary(doid = ignoreDoid, diagnosedYear = null))
        assertEvaluation(EvaluationResult.FAIL, functionWithMinDate.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
    }

    @Test
    fun `Should fail when prior tumors present in history with doid term not to ignore but outside date range to evaluate`(){
        val priorTumors = listOf(PriorTumorTestFactory.priorSecondPrimary(doid = "other", diagnosedYear = minDate.minusYears(3).year))
        assertEvaluation(EvaluationResult.FAIL, functionWithMinDate.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
    }
}
