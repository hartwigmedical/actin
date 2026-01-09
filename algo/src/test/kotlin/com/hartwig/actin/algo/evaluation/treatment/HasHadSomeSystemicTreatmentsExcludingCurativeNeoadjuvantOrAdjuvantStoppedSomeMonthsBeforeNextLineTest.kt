package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import org.junit.Test
import java.time.LocalDate

private const val MAX_MONTHS_BEFORE_NEXT_LINE = 3

class HasHadSomeSystemicTreatmentsExcludingCurativeNeoadjuvantOrAdjuvantStoppedSomeMonthsBeforeNextLineTest {

    private val referenceDate = LocalDate.of(2025, 12, 1)
    private val function =
        HasHadSomeSystemicTreatmentsExcludingCurativeNeoadjuvantOrAdjuvantStoppedSomeMonthsBeforeNextLine(2, MAX_MONTHS_BEFORE_NEXT_LINE, referenceDate)
    private val minimalOneLineFunction =
        HasHadSomeSystemicTreatmentsExcludingCurativeNeoadjuvantOrAdjuvantStoppedSomeMonthsBeforeNextLine(1, MAX_MONTHS_BEFORE_NEXT_LINE, referenceDate)

    @Test
    fun `Should pass when unknown intent systemic treatments reach threshold`() {
        val treatments = listOf("1", "2").map {
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment(it, isSystemic = true)), intents = null)
        }
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    @Test
    fun `Should pass when palliative intent systemic treatments reach threshold`() {
        val treatments = listOf("1", "2").map {
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.treatment(it, isSystemic = true)),
                intents = setOf(Intent.PALLIATIVE)
            )
        }
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    @Test
    fun `Should pass when threshold is reached by including curative or (neo)adjuvant intent systemic treatment stopped within max months before next line`() {
        listOf(Intent.CURATIVE, Intent.ADJUVANT, Intent.NEOADJUVANT).forEach { intent ->
            val treatments = listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("1", isSystemic = true)),
                    intents = setOf(intent),
                    stopYear = referenceDate.year,
                    stopMonth = referenceDate.monthValue - 2
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("2", isSystemic = true)),
                    intents = setOf(Intent.PALLIATIVE),
                    startYear = referenceDate.year,
                    startMonth = referenceDate.monthValue
                )
            )
            assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun `Should pass when threshold is reached by including curative or (neo)adjuvant intent systemic treatment stopped within max months before reference date when it is the latest line`() {
        listOf(Intent.CURATIVE, Intent.ADJUVANT, Intent.NEOADJUVANT).forEach { intent ->
            val treatments = listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("1", isSystemic = true)),
                    intents = setOf(intent),
                    stopYear = referenceDate.year,
                    stopMonth = referenceDate.monthValue - 2
                )
            )
            assertEvaluation(
                EvaluationResult.PASS,
                minimalOneLineFunction.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
            )
        }
    }

    @Test
    fun `Should fail when treatment history is empty`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail when history only contains non-systemic treatments`() {
        val treatments = listOf("1", "2").map {
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment(it, isSystemic = false)))
        }
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    @Test
    fun `Should fail when threshold is not reached because curative or (neo)adjuvant treatment is excluded from count since stopped more than min months before next line`() {
        listOf(Intent.CURATIVE, Intent.ADJUVANT, Intent.NEOADJUVANT).forEach { intent ->
            val treatments = listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("1", isSystemic = true)),
                    intents = setOf(intent),
                    stopYear = referenceDate.year,
                    stopMonth = referenceDate.monthValue - (MAX_MONTHS_BEFORE_NEXT_LINE + 1)
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("2", isSystemic = true)),
                    intents = setOf(Intent.PALLIATIVE),
                    startYear = referenceDate.year,
                    startMonth = referenceDate.monthValue
                )
            )
            assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun `Should fail when threshold is not reached because curative or (neo)adjuvant treatment is excluded from count since stopped more than min months before reference date when it is the latest line`() {
        listOf(Intent.CURATIVE, Intent.ADJUVANT, Intent.NEOADJUVANT).forEach { intent ->
            val treatments = listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("1", isSystemic = true)),
                    intents = setOf(intent),
                    stopYear = referenceDate.year,
                    stopMonth = referenceDate.monthValue - (MAX_MONTHS_BEFORE_NEXT_LINE + 1)
                )
            )
            assertEvaluation(EvaluationResult.FAIL, minimalOneLineFunction.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun `Should fail when threshold is not reached because curative or (neo)adjuvant treatment is excluded from count since inferred stop date is too long before next line`() {
        listOf(Intent.CURATIVE, Intent.ADJUVANT, Intent.NEOADJUVANT).forEach { intent ->
            val treatments = listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("1", isSystemic = true)),
                    intents = setOf(intent),
                    startYear = referenceDate.year - 2
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("2", isSystemic = true)),
                    intents = setOf(Intent.PALLIATIVE),
                    startYear = referenceDate.year,
                    startMonth = referenceDate.monthValue
                )
            )
            assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun `Should fail when threshold is not reached because curative or (neo)adjuvant treatment is excluded from count since inferred stop date is too long before reference date when it is the latest line`() {
        listOf(Intent.CURATIVE, Intent.ADJUVANT, Intent.NEOADJUVANT).forEach { intent ->
            val treatments = listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("1", isSystemic = true)),
                    intents = setOf(intent),
                    startYear = referenceDate.year - 2
                )
            )
            assertEvaluation(EvaluationResult.FAIL, minimalOneLineFunction.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun `Should evaluate to undetermined when uncertain if curative or (neo)adjuvant treatment counts for threshold due to ambiguous timeline`() {
        listOf(Intent.CURATIVE, Intent.ADJUVANT, Intent.NEOADJUVANT).forEach { intent ->
            val treatments = listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("1", isSystemic = true)),
                    intents = setOf(intent),
                    startYear = null,
                    stopYear = null
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("2", isSystemic = true)),
                    intents = setOf(Intent.PALLIATIVE),
                    startYear = referenceDate.year,
                    startMonth = referenceDate.monthValue
                )
            )
            assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun `Should evaluate to undetermined when unclear if curative or (neo)adjuvant treatment counts for threshold since stop month is missing and date range has conflicting results`() {
        listOf(Intent.CURATIVE, Intent.ADJUVANT, Intent.NEOADJUVANT).forEach { intent ->
            val treatments = listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("1", isSystemic = true)),
                    intents = setOf(intent),
                    stopYear = referenceDate.year,
                    stopMonth = null
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("2", isSystemic = true)),
                    intents = setOf(Intent.PALLIATIVE),
                    startYear = referenceDate.year,
                    startMonth = referenceDate.monthValue
                )
            )
            assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun `Should evaluate to undetermined when unclear if curative or (neo)adjuvant treatment counts for threshold since start date is missing and inferred stop date is not more than minimum months before next line`() {
        listOf(Intent.CURATIVE, Intent.ADJUVANT, Intent.NEOADJUVANT).forEach { intent ->
            val treatments = listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("1", isSystemic = true)),
                    intents = setOf(intent),
                    stopYear = null,
                    stopMonth = null,
                    startYear = referenceDate.year,
                    startMonth = referenceDate.monthValue - 5
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("2", isSystemic = true)),
                    intents = setOf(Intent.PALLIATIVE),
                    startYear = referenceDate.year,
                    startMonth = referenceDate.monthValue
                )
            )
            assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }
}