package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import org.junit.Test
import java.time.LocalDate

private const val MAX_MONTHS_BEFORE_NEXT_LINE = 3

class HasHadLimitedSystemicTreatmentsExcludingCurativeNeoadjuvantOrAdjuvantStartedSomeMonthsBeforeNextLineTest {

    private val referenceDate = LocalDate.of(2025, 12, 1)
    private val function =
        HasHadLimitedSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonths(
            1,
            MAX_MONTHS_BEFORE_NEXT_LINE,
            referenceDate
        )
    private val maxZeroLinesFunction =
        HasHadLimitedSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonths(
            0,
            MAX_MONTHS_BEFORE_NEXT_LINE,
            referenceDate
        )

    @Test
    fun `Should pass when treatment history is empty`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should pass when history only contains non-systemic treatments`() {
        val treatments = listOf("1", "2").map {
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.treatment(it, isSystemic = false))
            )
        }
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    @Test
    fun `Should pass when unknown intent systemic treatments do no exceed the limit`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TreatmentTestFactory.withTreatmentHistoryEntry(
                    TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("", isSystemic = true)), intents = null)
                )
            )
        )
    }

    @Test
    fun `Should pass when palliative intent systemic treatments do no exceed the limit`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TreatmentTestFactory.withTreatmentHistoryEntry(
                    TreatmentTestFactory.treatmentHistoryEntry(
                        setOf(TreatmentTestFactory.treatment("", isSystemic = true)),
                        intents = setOf(Intent.PALLIATIVE)
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass when lines exceed the limit but curative or (neo)adjuvant treatment is excluded in count since stopped more than max months before next line`() {
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
            assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun `Should fail when palliative systemic treatments exceed the limit`() {
        val treatments = listOf("1", "2").map {
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.treatment(it, isSystemic = true)), intents = setOf(Intent.PALLIATIVE)
            )
        }
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    @Test
    fun `Should fail when limit is exceeded due to inclusion of curative or (neo)adjuvant treatment in count since stopped within max months before next line`() {
        listOf(Intent.CURATIVE, Intent.ADJUVANT, Intent.NEOADJUVANT).forEach { intent ->
            val treatments = listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("1", isSystemic = true)),
                    intents = setOf(intent),
                    stopYear = referenceDate.year,
                    stopMonth = referenceDate.monthValue - (MAX_MONTHS_BEFORE_NEXT_LINE - 1)
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("", isSystemic = true)),
                    intents = setOf(Intent.PALLIATIVE),
                    startYear = referenceDate.year,
                    startMonth = referenceDate.monthValue
                )
            )
            assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun `Should fail when limit is exceeded due to inclusion of curative or (neo)adjuvant treatment in count since stopped within max months before reference date when it is the latest line`() {
        listOf(Intent.CURATIVE, Intent.ADJUVANT, Intent.NEOADJUVANT).forEach { intent ->
            val treatments = listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("1", isSystemic = true)),
                    intents = setOf(intent),
                    stopYear = referenceDate.year,
                    stopMonth = referenceDate.monthValue - (MAX_MONTHS_BEFORE_NEXT_LINE - 1)
                )
            )
            assertEvaluation(EvaluationResult.FAIL, maxZeroLinesFunction.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun `Should evaluate to undetermined when unknown if limit is exceeded since uncertain if curative or (neo)adjuvant treatment counts due to missing dates`() {
        listOf(Intent.CURATIVE, Intent.ADJUVANT, Intent.NEOADJUVANT).forEach { intent ->
            val treatments = listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("1", isSystemic = true)),
                    intents = setOf(intent),
                    startYear = null,
                    startMonth = null,
                    stopYear = null,
                    stopMonth = null
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("2", isSystemic = true)),
                    intents = setOf(Intent.PALLIATIVE),
                    startYear = referenceDate.year,
                    startMonth = referenceDate.monthValue
                )
            )
            assertEvaluation(
                EvaluationResult.UNDETERMINED,
                function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
            )
        }
    }

    @Test
    fun `Should evaluate to undetermined when unknown if limit is exceeded since uncertain if curative or (neo)adjuvant treatment counts due to unclear timeline`() {
        listOf(Intent.CURATIVE, Intent.ADJUVANT, Intent.NEOADJUVANT).forEach { intent ->
            val treatments = listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("1", isSystemic = true)),
                    intents = setOf(intent),
                    startYear = null,
                    startMonth = null,
                    stopYear = null,
                    stopMonth = null
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.treatment("2", isSystemic = true)),
                    intents = setOf(Intent.PALLIATIVE),
                    startYear = null,
                    startMonth = null
                )
            )
            assertEvaluation(
                EvaluationResult.UNDETERMINED,
                function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
            )
        }
    }

    @Test
    fun `Should evaluate to undetermined when unknown if limit is exceeded since uncertain if curative or (neo)adjuvant treatment counts due to conflicting date range results`() {
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
            assertEvaluation(
                EvaluationResult.UNDETERMINED,
                function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
            )
        }
    }
}