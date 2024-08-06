package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import org.junit.Test
import java.time.LocalDate

class HasHadAdjuvantTreatmentWithCategoryTest {

    private val matchCategory = TreatmentCategory.TARGETED_THERAPY
    private val referenceDate = LocalDate.of(2024, 7, 30)
    private val minDate = referenceDate.minusMonths(6)
    private val recentDate = referenceDate.minusMonths(3)
    private val olderDate = referenceDate.minusMonths(14)
    private val functionWithDate = HasHadAdjuvantTreatmentWithCategory(matchCategory, minDate, 6)
    private val functionWithoutDate = HasHadAdjuvantTreatmentWithCategory(matchCategory, null, null)

    @Test
    fun `Should fail with no treatment history`() {
        assertEvaluation(EvaluationResult.FAIL, functionWithDate.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
        assertEvaluation(EvaluationResult.FAIL, functionWithoutDate.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for adjuvant treatment without matching category`() {
        assertResultForCategoryAndIntent(
            EvaluationResult.FAIL,
            TreatmentCategory.IMMUNOTHERAPY,
            setOf(Intent.ADJUVANT),
            recentDate,
            functionWithDate
        )
        assertResultForCategoryAndIntent(
            EvaluationResult.FAIL,
            TreatmentCategory.IMMUNOTHERAPY,
            setOf(Intent.ADJUVANT),
            recentDate,
            functionWithoutDate
        )
    }

    @Test
    fun `Should fail for non adjuvant treatment with matching category`() {
        assertResultForCategoryAndIntent(EvaluationResult.FAIL, matchCategory, emptySet(), recentDate, functionWithoutDate)
    }

    @Test
    fun `Should fail for neoadjuvant treatment with matching category`() {
        assertResultForCategoryAndIntent(EvaluationResult.FAIL, matchCategory, setOf(Intent.NEOADJUVANT), recentDate, functionWithoutDate)
    }

    @Test
    fun `Should be undetermined with adjuvant treatment with matching category but without (stop)date`() {
        assertResultForCategoryAndIntent(EvaluationResult.UNDETERMINED, matchCategory, setOf(Intent.ADJUVANT), null, functionWithDate)
    }

    @Test
    fun `Should fail with adjuvant treatment with matching category but too long ago`() {
        assertResultForCategoryAndIntent(EvaluationResult.FAIL, matchCategory, setOf(Intent.NEOADJUVANT), olderDate, functionWithDate)
    }

    @Test
    fun `Should pass for adjuvant treatment with matching category and correct date`() {
        assertResultForCategoryAndIntent(EvaluationResult.PASS, matchCategory, setOf(Intent.ADJUVANT), recentDate, functionWithDate)
    }

    @Test
    fun `Should pass for neoadjuvant and adjuvant treatment with matching category and correct date`() {
        assertResultForCategoryAndIntent(
            EvaluationResult.PASS,
            matchCategory,
            setOf(Intent.NEOADJUVANT, Intent.ADJUVANT),
            recentDate,
            functionWithDate
        )
    }

    private fun assertResultForCategoryAndIntent(
        expectedResult: EvaluationResult,
        category: TreatmentCategory,
        intents: Set<Intent>,
        date: LocalDate?,
        function: HasHadAdjuvantTreatmentWithCategory
    ) {
        val treatment = TreatmentTestFactory.drugTreatment("drug therapy", category)
        val record =
            TreatmentTestFactory.withTreatmentHistoryEntry(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(treatment),
                    intents = intents,
                    stopYear = date?.year,
                    stopMonth = date?.monthValue
                )
            )
        assertEvaluation(expectedResult, function.evaluate(record))
    }
}