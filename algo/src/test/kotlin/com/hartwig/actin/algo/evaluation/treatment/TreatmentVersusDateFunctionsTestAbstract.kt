package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate
import org.junit.jupiter.api.Test

private const val YEARS_TO_SUBTRACT = 3

private val REFERENCE_DATE = LocalDate.of(2025, 6, 1)
private val TARGET_DATE = REFERENCE_DATE.minusYears(1)
private val RECENT_DATE = TARGET_DATE.plusYears(1)
private val OLDER_DATE = TARGET_DATE.minusYears(1)
private val NON_MATCHING_RECENT_TREATMENT = treatmentHistoryEntry(
    setOf(treatment("other", false)), startYear = RECENT_DATE.year, startMonth = RECENT_DATE.monthValue
)
private val NON_MATCHING_OLDER_TREATMENT = treatmentHistoryEntry(
    setOf(treatment("other", false)), startYear = OLDER_DATE.year, startMonth = OLDER_DATE.monthValue
)
private val NON_MATCHING_TREATMENT_NO_DATE = treatmentHistoryEntry(
    setOf(treatment("other", false)), startYear = null, startMonth = null
)

abstract class TreatmentVersusDateFunctionsTestAbstract {

    abstract fun functionForDate(minDate: LocalDate): EvaluationFunction

    abstract fun matchingTreatment(
        stopYear: Int?, stopMonth: Int?, startYear: Int? = null, startMonth: Int? = null
    ): TreatmentHistoryEntry

    @Test
    fun `Should fail when treatment not found`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function().evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(NON_MATCHING_RECENT_TREATMENT))),
            "No treatments matching 'Treatment' in history"
        )
    }

    @Test
    fun `Should fail when matching treatment is older by year`() {
        val treatmentHistory = listOf(matchingOlderTreatment())
        assertEvaluation(
            EvaluationResult.FAIL,
            function().evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "All treatments matching 'Treatment' administered before 01-Jun-2024"
        )
    }

    @Test
    fun `Should fail when matching treatment is older by month`() {
        val olderDate = TARGET_DATE.minusMonths(1)
        val treatmentHistory = listOf(
            NON_MATCHING_RECENT_TREATMENT,
            matchingTreatment(startYear = olderDate.year - 1, stopYear = olderDate.year, stopMonth = olderDate.monthValue)
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function().evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "All treatments matching 'Treatment' administered before 01-Jun-2024"
        )
    }

    @Test
    fun `Should fail when matching treatment has unknown start date and known stop date before min date`() {
        val treatmentHistory = listOf(matchingTreatment(stopYear = OLDER_DATE.year, stopMonth = OLDER_DATE.monthValue))
        assertEvaluation(
            EvaluationResult.FAIL,
            function().evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "All treatments matching 'Treatment' administered before 01-Jun-2024"
        )
    }

    @Test
    fun `Should be undetermined when most recent line stops in min date year with unknown stop month`() {
        val treatmentHistory = listOf(
            matchingTreatment(
                stopYear = TARGET_DATE.year, stopMonth = null, startYear = OLDER_DATE.year, startMonth = OLDER_DATE.monthValue
            )
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function().evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "Undetermined if treatment matching 'Treatment' may have been administered since 01-Jun-2024 because it is the last treatment line and stop date missing"
        )
    }

    @Test
    fun `Should pass when treatment history includes matching treatment within range`() {
        val treatmentHistory =
            listOf(NON_MATCHING_RECENT_TREATMENT, matchingOlderTreatment(), matchingTreatment(RECENT_DATE.year, RECENT_DATE.monthValue))
        assertEvaluation(
            EvaluationResult.PASS,
            function().evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "Treatment matching 'Treatment' administered since 01-Jun-2024"
        )
    }

    @Test
    fun `Should be undetermined when matching treatment has unknown start and stop year`() {
        val treatmentHistory = listOf(NON_MATCHING_RECENT_TREATMENT, matchingOlderTreatment(), matchingTreatment(null, 10))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function().evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "Treatment matching 'Treatment' administered with unknown date hence undetermined if administered since 01-Jun-2024"
        )
    }

    @Test
    fun `Should be undetermined when matching treatment matches year with unknown month`() {
        val treatmentHistory = listOf(NON_MATCHING_RECENT_TREATMENT, matchingTreatment(TARGET_DATE.year, null))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function().evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "Treatment matching 'Treatment' administered with unknown date hence undetermined if administered since 01-Jun-2024"
        )
    }

    @Test
    fun `Should pass when prior treatment has unknown stop date but start date within range`() {
        val treatmentHistory = listOf(
            NON_MATCHING_RECENT_TREATMENT,
            matchingTreatment(startYear = RECENT_DATE.year, startMonth = RECENT_DATE.monthValue, stopYear = null, stopMonth = null)
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function().evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "Treatment matching 'Treatment' administered since 01-Jun-2024"
        )
    }

    @Test
    fun `Should fail when prior treatment has unknown stop date and older start date and not most recent treatment line`() {
        val olderDate = REFERENCE_DATE.minusYears(YEARS_TO_SUBTRACT.toLong())
        val treatmentHistory = listOf(NON_MATCHING_RECENT_TREATMENT, matchingTreatment(null, null, olderDate.year, olderDate.monthValue))
        assertEvaluation(
            EvaluationResult.FAIL,
            function().evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "All treatments matching 'Treatment' administered before 01-Jun-2024"
        )
    }

    @Test
    fun `Should fail when matching treatment is most recent line but stop date known and before min date`() {
        val treatmentHistory = listOf(
            matchingTreatment(
                startYear = OLDER_DATE.minusYears(1).year,
                startMonth = OLDER_DATE.monthValue,
                stopYear = OLDER_DATE.year,
                stopMonth = OLDER_DATE.monthValue
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function().evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "All treatments matching 'Treatment' administered before 01-Jun-2024"
        )
    }

    @Test
    fun `Should be undetermined when matching treatment has unknown stop date and older start date but is most recent line`() {
        val treatmentHistory = listOf(
            NON_MATCHING_OLDER_TREATMENT,
            matchingTreatment(startYear = OLDER_DATE.year, startMonth = OLDER_DATE.monthValue + 1, stopYear = null, stopMonth = null)
        )
        val evaluation = function().evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Undetermined if treatment matching 'Treatment' may have been administered since 01-Jun-2024 because it is the last treatment line and stop date missing"
        )
    }

    @Test
    fun `Should be undetermined when matching treatment is most recent line but there are treatments with unknown date`() {
        val treatmentHistory = listOf(
            NON_MATCHING_TREATMENT_NO_DATE,
            matchingTreatment(startYear = OLDER_DATE.year, startMonth = OLDER_DATE.monthValue + 1, stopYear = null, stopMonth = null)
        )
        val evaluation = function().evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Undetermined if treatment matching 'Treatment' may have been administered since 01-Jun-2024 because it is the last treatment line and stop date missing"
        )
    }

    private fun function() = functionForDate(TARGET_DATE)

    private fun matchingOlderTreatment() = matchingTreatment(
        startYear = REFERENCE_DATE.minusYears((YEARS_TO_SUBTRACT + 1).toLong()).year,
        stopYear = REFERENCE_DATE.minusYears(YEARS_TO_SUBTRACT.toLong()).year,
        stopMonth = null
    )
}