package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import java.time.LocalDate
import org.junit.jupiter.api.Test

private const val ignoreDoid = "ignore doid"
private const val ignoreTerm = "ignore term"
private const val ignoreName = "ignore name"
private const val parentDoid = "parent doid"
private const val parentTerm = "parent term"
private const val otherName = "other name"

class HasHistoryOfSecondMalignancyIgnoringDoidTermsTest {

    private val minDate = LocalDate.of(2024, 4, 22)
    private val doidModel = TestDoidModelFactory.createWithParentChildAndTermPerDoidMaps(
        mapOf(ignoreDoid to parentDoid),
        mapOf(ignoreDoid to ignoreTerm, parentDoid to parentTerm),
    )
    private val functionWithoutMinDate = HasHistoryOfSecondMalignancyIgnoringDoidTerms(doidModel, setOf(ignoreDoid), minDate = null)
    private val functionWithMinDate = HasHistoryOfSecondMalignancyIgnoringDoidTerms(doidModel, setOf(ignoreDoid), minDate = minDate)

    @Test
    fun `Should fail when no prior tumors present`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithoutMinDate.evaluate(PriorTumorTestFactory.withPriorPrimaries(emptyList())),
            "No history of other malignancy"
        )
    }

    @Test
    fun `Should fail when prior tumors present in history but with doid to ignore`() {
        val priorTumors = listOf(PriorTumorTestFactory.priorPrimary(doid = ignoreDoid, name = ignoreName))
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithoutMinDate.evaluate(PriorTumorTestFactory.withPriorPrimaries(priorTumors)),
            "No history of previous malignancy excluding ignore name"
        )
    }

    @Test
    fun `Should fail when prior tumors present in history but doid is child of doid to ignore`() {
        val priorTumors = listOf(PriorTumorTestFactory.priorPrimary(doid = ignoreDoid, name = ignoreName))
        val function = HasHistoryOfSecondMalignancyIgnoringDoidTerms(doidModel, setOf(parentDoid), minDate = null)
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(PriorTumorTestFactory.withPriorPrimaries(priorTumors)),
            "No history of previous malignancy excluding ignore name"
        )
    }

    @Test
    fun `Should pass when prior tumors present in history with doid term not to ignore`() {
        val priorTumors =
            listOf(PriorTumorTestFactory.priorPrimary(doid = "other", name = otherName, diagnosedYear = minDate.year))
        assertEvaluation(
            EvaluationResult.PASS,
            functionWithoutMinDate.evaluate(PriorTumorTestFactory.withPriorPrimaries(priorTumors)),
            "Has history of previous malignancy (other name)"
        )
    }

    @Test
    fun `Should pass when prior tumors present in history with doid term not to ignore and within requested date range`() {
        val priorTumors =
            listOf(PriorTumorTestFactory.priorPrimary(doid = "other", name = otherName, diagnosedYear = minDate.year))
        assertEvaluation(
            EvaluationResult.PASS,
            functionWithMinDate.evaluate(PriorTumorTestFactory.withPriorPrimaries(priorTumors)),
            "Has history of recent previous malignancy (other name)"
        )
    }

    @Test
    fun `Should evaluate to undetermined when prior tumors present in history with doid term not to ignore but date unknown`() {
        val priorTumors = listOf(
            PriorTumorTestFactory.priorPrimary(
                doid = "other",
                name = otherName,
                diagnosedYear = null,
                diagnosedMonth = null,
                lastTreatmentMonth = null,
                lastTreatmentYear = null
            )
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED, functionWithMinDate.evaluate(PriorTumorTestFactory.withPriorPrimaries(priorTumors)),
            "Has history of previous malignancy (other name) but undetermined if recent (date unknown)"
        )
    }

    @Test
    fun `Should fail when prior tumors with doid term to ignore and unknown date in history`() {
        val priorTumors =
            listOf(PriorTumorTestFactory.priorPrimary(doid = ignoreDoid, name = ignoreName, diagnosedYear = null))
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithMinDate.evaluate(PriorTumorTestFactory.withPriorPrimaries(priorTumors)),
            "No recent history of other malignancy"
        )
    }

    @Test
    fun `Should fail when prior tumors present in history with doid term not to ignore but outside date range to evaluate`() {
        val priorTumors = listOf(
            PriorTumorTestFactory.priorPrimary(
                doid = "other",
                name = otherName,
                diagnosedYear = minDate.minusYears(3).year
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithMinDate.evaluate(PriorTumorTestFactory.withPriorPrimaries(priorTumors)),
            "No recent history of other malignancy"
        )
    }
}
