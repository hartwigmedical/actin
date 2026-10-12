package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

private val MIN_DATE = LocalDate.of(2024, 6, 1)
private val RECENT_DATE = MIN_DATE.plusYears(1)
private val OLDER_DATE = MIN_DATE.minusYears(1)
private val NON_MATCHING_RECENT_TREATMENT = treatmentHistoryEntry(
    setOf(treatment("other", false)), startYear = RECENT_DATE.year, startMonth = RECENT_DATE.monthValue
)
private val NON_MATCHING_TREATMENT_NO_DATE = treatmentHistoryEntry(
    setOf(treatment("other", false)), startYear = null, startMonth = null
)

class HasHadSpecificTreatmentSinceDateTest {

    private val treatmentQuery = DrugTreatment(
        name = "treatment",
        drugs = setOf(Drug(name = "Chemo drug", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = emptySet()))
    )
    private val function = HasHadSpecificTreatmentSinceDate(treatmentQuery, MIN_DATE)

    @Test
    fun `Should fail when treatment not found`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(NON_MATCHING_RECENT_TREATMENT))),
            "No treatments matching 'Treatment' in history"
        )
    }

    @Test
    fun `Should fail when matching treatment is older by year`() {
        val treatmentHistory = listOf(matchingOlderTreatment())
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "All treatments matching 'Treatment' administered before 01-Jun-2024"
        )
    }

    @Test
    fun `Should fail when matching treatment is older by month`() {
        val olderDate = MIN_DATE.minusMonths(1)
        val treatmentHistory = listOf(
            NON_MATCHING_RECENT_TREATMENT,
            matchingTreatment(startYear = olderDate.year - 1, stopYear = olderDate.year, stopMonth = olderDate.monthValue)
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "All treatments matching 'Treatment' administered before 01-Jun-2024"
        )
    }

    @Test
    fun `Should fail when matching treatment has unknown start date and known stop date before min date`() {
        val treatmentHistory = listOf(matchingTreatment(stopYear = OLDER_DATE.year, stopMonth = OLDER_DATE.monthValue))
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "All treatments matching 'Treatment' administered before 01-Jun-2024"
        )
    }

    @Test
    fun `Should be undetermined when most recent line stops in min date year with unknown stop month`() {
        val treatmentHistory = listOf(
            matchingTreatment(
                stopYear = MIN_DATE.year, stopMonth = null, startYear = OLDER_DATE.year, startMonth = OLDER_DATE.monthValue
            )
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "Undetermined if treatment matching 'Treatment' may have been administered since 01-Jun-2024 (missing stop date)"
        )
    }

    @Test
    fun `Should pass when treatment history includes matching treatment within range`() {
        val treatmentHistory =
            listOf(
                NON_MATCHING_RECENT_TREATMENT,
                matchingOlderTreatment(),
                matchingTreatment(stopYear = RECENT_DATE.year, stopMonth = RECENT_DATE.monthValue)
            )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "Treatment matching 'Treatment' administered since 01-Jun-2024"
        )
    }

    @Test
    fun `Should be undetermined when all dates are unknown`() {
        val treatmentHistory = listOf(NON_MATCHING_RECENT_TREATMENT, matchingTreatment(stopYear = null, stopMonth = null))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "Undetermined if treatment matching 'Treatment' may have been administered since 01-Jun-2024 (missing stop date)"
        )
    }

    @Test
    fun `Should be undetermined when matching treatment has unknown start and stop year`() {
        val treatmentHistory =
            listOf(NON_MATCHING_RECENT_TREATMENT, matchingOlderTreatment(), matchingTreatment(stopYear = null, stopMonth = 10))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "Undetermined if treatment matching 'Treatment' may have been administered since 01-Jun-2024 (missing stop date)"
        )
    }

    @Test
    fun `Should be undetermined when matching treatment matches year with unknown month`() {
        val treatmentHistory = listOf(NON_MATCHING_RECENT_TREATMENT, matchingTreatment(stopYear = MIN_DATE.year, stopMonth = null))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "Undetermined if treatment matching 'Treatment' may have been administered since 01-Jun-2024 (missing stop date)"
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
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "Treatment matching 'Treatment' administered since 01-Jun-2024"
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
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "All treatments matching 'Treatment' administered before 01-Jun-2024"
        )
    }

    @Test
    fun `Should be undetermined when matching treatment has older start date but max stop date is after min date`() {
        val treatmentHistory = listOf(
            NON_MATCHING_TREATMENT_NO_DATE,
            matchingTreatment(
                startYear = OLDER_DATE.year,
                startMonth = OLDER_DATE.monthValue + 1,
                stopYear = null,
                stopMonth = null,
                maxStopYear = RECENT_DATE.year,
                maxStopMonth = RECENT_DATE.monthValue
            )
        )
        val evaluation = function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Undetermined if treatment matching 'Treatment' may have been administered since 01-Jun-2024 (missing stop date)"
        )
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly(
            "Undetermined if treatment matching 'Treatment' may have been administered since 01-Jun-2024 (missing stop date)"
        )
    }

    @Test
    fun `Should fail when prior treatment has unknown stop date but older start date and max stop date`() {
        val treatmentHistory = listOf(
            NON_MATCHING_RECENT_TREATMENT,
            matchingTreatment(
                startYear = OLDER_DATE.year,
                startMonth = OLDER_DATE.monthValue,
                stopYear = null,
                stopMonth = null,
                maxStopYear = OLDER_DATE.year,
                maxStopMonth = OLDER_DATE.monthValue
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)),
            "All treatments matching 'Treatment' administered before 01-Jun-2024"
        )
    }

    private fun matchingOlderTreatment() = matchingTreatment(
        startYear = MIN_DATE.year,
        startMonth = MIN_DATE.monthValue,
        stopYear = OLDER_DATE.year,
        stopMonth = OLDER_DATE.monthValue,
    )

    private fun matchingTreatment(
        startYear: Int? = null,
        startMonth: Int? = null,
        stopYear: Int? = null,
        stopMonth: Int? = null,
        maxStopYear: Int? = null,
        maxStopMonth: Int? = null
    ): TreatmentHistoryEntry {
        return treatmentHistoryEntry(
            setOf(treatmentQuery),
            startYear = startYear,
            startMonth = startMonth,
            stopYear = stopYear,
            stopMonth = stopMonth,
            maxStopYear = maxStopYear,
            maxStopMonth = maxStopMonth
        )
    }
}