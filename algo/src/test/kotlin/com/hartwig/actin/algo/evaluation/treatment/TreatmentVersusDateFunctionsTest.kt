package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.algo.evaluation.treatment.TreatmentVersusDateFunctions.certainTreatmentSinceMinDate
import com.hartwig.actin.algo.evaluation.treatment.TreatmentVersusDateFunctions.partitionTreatmentsByCertainOccurrenceSinceMinDate
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

private val TARGET_DATE = LocalDate.of(2025, 6, 1)
private val RECENT_DATE = TARGET_DATE.plusYears(1)
private val OLDER_DATE = TARGET_DATE.minusYears(1)
private val TREATMENT = DrugTreatment(
    name = "treatment",
    drugs = setOf(Drug(name = "drug", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = emptySet()))
)

class TreatmentVersusDateFunctionsTest {

    @Test
    fun `Should return true for certainTreatmentSinceMinDate if stop date is after target date`() {
        val treatment = treatment(stopYear = RECENT_DATE.year, stopMonth = RECENT_DATE.monthValue)
        assertThat(certainTreatmentSinceMinDate(treatment, TARGET_DATE)).isTrue
    }

    @Test
    fun `Should return false for certainTreatmentSinceMinDate if stop date is before target date`() {
        val treatment = treatment(stopYear = OLDER_DATE.year, stopMonth = OLDER_DATE.monthValue)
        assertThat(certainTreatmentSinceMinDate(treatment, TARGET_DATE)).isFalse
    }

    @Test
    fun `Should return true for certainTreatmentSinceMinDate if stop date is unknown but start date is after target date`() {
        val treatment = treatment(startYear = RECENT_DATE.year, startMonth = RECENT_DATE.monthValue)
        assertThat(certainTreatmentSinceMinDate(treatment, TARGET_DATE)).isTrue
    }

    @Test
    fun `Should return false for certainTreatmentSinceMinDate if stop date is unknown and start date is before target date`() {
        val treatment = treatment(startYear = OLDER_DATE.year, startMonth = OLDER_DATE.monthValue)
        assertThat(certainTreatmentSinceMinDate(treatment, TARGET_DATE)).isFalse
    }

    @Test
    fun `Should return false for certainTreatmentSinceMinDate if both stop date and start date are unknown`() {
        val treatment = treatment()
        assertThat(certainTreatmentSinceMinDate(treatment, TARGET_DATE)).isFalse
    }

    @Test
    fun `Should return true for potentialTreatmentSinceMinDate if stop date is after target date`() {
        val treatment = treatment(stopYear = RECENT_DATE.year, stopMonth = RECENT_DATE.monthValue)
        assertThat(TreatmentVersusDateFunctions.potentialTreatmentSinceMinDate(treatment, TARGET_DATE)).isTrue
    }

    @Test
    fun `Should return false for potentialTreatmentSinceMinDate if stop date is before target date`() {
        val treatment = treatment(stopYear = OLDER_DATE.year, stopMonth = OLDER_DATE.monthValue)
        assertThat(TreatmentVersusDateFunctions.potentialTreatmentSinceMinDate(treatment, TARGET_DATE)).isFalse
    }

    @Test
    fun `Should return true for potentialTreatmentSinceMinDate if stop date is unknown`() {
        val treatment = treatment()
        assertThat(TreatmentVersusDateFunctions.potentialTreatmentSinceMinDate(treatment, TARGET_DATE)).isTrue
    }

    @Test
    fun `Should return true for potentialTreatmentSinceMinDate if stop date is unknown but maxStopDate is after target date`() {
        val treatment = treatment(maxStopYear = RECENT_DATE.year, maxStopMonth = RECENT_DATE.monthValue)
        assertThat(TreatmentVersusDateFunctions.potentialTreatmentSinceMinDate(treatment, TARGET_DATE)).isTrue
    }

    @Test
    fun `Should return false for potentialTreatmentSinceMinDate if stop date is unknown but maxStopDate is before target date`() {
        val treatment = treatment(maxStopYear = OLDER_DATE.year, maxStopMonth = OLDER_DATE.monthValue)
        assertThat(TreatmentVersusDateFunctions.potentialTreatmentSinceMinDate(treatment, TARGET_DATE)).isFalse
    }

    @Test
    fun `Should place treatments in partitionTreatmentsByCertainOccurrenceSinceMinDate correctly`() {
        val recent = treatmentHistoryEntry(
            setOf(TREATMENT),
            stopYear = RECENT_DATE.year,
            stopMonth = RECENT_DATE.monthValue
        )
        val recentPotentially = treatmentHistoryEntry(
            setOf(TREATMENT),
            maxStopYear = RECENT_DATE.year,
            maxStopMonth = RECENT_DATE.monthValue
        )
        val nonRecent = treatmentHistoryEntry(
            setOf(TREATMENT),
            stopYear = OLDER_DATE.year,
            stopMonth = OLDER_DATE.monthValue
        )
        val treatments = listOf(recent, recentPotentially, nonRecent)

        val (certainRecent, other) = treatments.partitionTreatmentsByCertainOccurrenceSinceMinDate(TARGET_DATE)
        assertThat(certainRecent).containsExactly(recent)
        assertThat(other).containsExactly(recentPotentially, nonRecent)
    }

    @Test
    fun `Should return true for certainTreatmentBeforeMaxDate if start date is before target date`() {
        val treatment = treatment(startYear = OLDER_DATE.year, startMonth = OLDER_DATE.monthValue)
        assertThat(TreatmentVersusDateFunctions.certainTreatmentBeforeMaxDate(treatment, TARGET_DATE)).isTrue
    }

    @Test
    fun `Should return false for certainTreatmentBeforeMaxDate if start date is after target date`() {
        val treatment = treatment(startYear = RECENT_DATE.year, startMonth = RECENT_DATE.monthValue)
        assertThat(TreatmentVersusDateFunctions.certainTreatmentBeforeMaxDate(treatment, TARGET_DATE)).isFalse
    }

    @Test
    fun `Should return true for certainTreatmentBeforeMaxDate if stop date is before target date`() {
        val treatment = treatment(stopYear = OLDER_DATE.year, stopMonth = OLDER_DATE.monthValue)
        assertThat(TreatmentVersusDateFunctions.certainTreatmentBeforeMaxDate(treatment, TARGET_DATE)).isTrue
    }

    @Test
    fun `Should return false for certainTreatmentBeforeMaxDate if stop date is after target date`() {
        val treatment = treatment(stopYear = RECENT_DATE.year, stopMonth = RECENT_DATE.monthValue)
        assertThat(TreatmentVersusDateFunctions.certainTreatmentBeforeMaxDate(treatment, TARGET_DATE)).isFalse
    }

    @Test
    fun `Should return false for potentialTreatmentBeforeMaxDate if start date is after target date`() {
        val treatment = treatment(startYear = RECENT_DATE.year, startMonth = RECENT_DATE.monthValue)
        assertThat(TreatmentVersusDateFunctions.potentialTreatmentBeforeMaxDate(treatment, TARGET_DATE)).isFalse
    }

    @Test
    fun `Should return true for potentialTreatmentBeforeMaxDate if start date is before target date`() {
        val treatment = treatment(startYear = OLDER_DATE.year, startMonth = OLDER_DATE.monthValue)
        assertThat(TreatmentVersusDateFunctions.potentialTreatmentBeforeMaxDate(treatment, TARGET_DATE)).isTrue
    }

    @Test
    fun `Should return true for potentialTreatmentBeforeMaxDate if stop date is before target date`() {
        val treatment = treatment(stopYear = OLDER_DATE.year, stopMonth = OLDER_DATE.monthValue)
        assertThat(TreatmentVersusDateFunctions.potentialTreatmentBeforeMaxDate(treatment, TARGET_DATE)).isTrue
    }

    @Test
    fun `Should return true for potentialTreatmentBeforeMaxDate if stop date is after target date`() {
        val treatment = treatment(stopYear = RECENT_DATE.year, stopMonth = RECENT_DATE.monthValue)
        assertThat(TreatmentVersusDateFunctions.potentialTreatmentBeforeMaxDate(treatment, TARGET_DATE)).isTrue
    }

    @Test
    fun `Should return true for potentialTreatmentBeforeMaxDate if all dates unknown`() {
        val treatment = treatment()
        assertThat(TreatmentVersusDateFunctions.potentialTreatmentBeforeMaxDate(treatment, TARGET_DATE)).isTrue
    }

    @Test
    fun `Should return true for potentialTreatmentBeforeMaxDate if maxStopDate is before target date`() {
        val treatment = treatment(maxStopYear = OLDER_DATE.year, maxStopMonth = OLDER_DATE.monthValue)
        assertThat(TreatmentVersusDateFunctions.potentialTreatmentBeforeMaxDate(treatment, TARGET_DATE)).isTrue
    }

    @Test
    fun `Should return true for potentialTreatmentBeforeMaxDate if maxStopDate is after target date`() {
        val treatment = treatment(maxStopYear = RECENT_DATE.year, maxStopMonth = RECENT_DATE.monthValue)
        assertThat(TreatmentVersusDateFunctions.potentialTreatmentBeforeMaxDate(treatment, TARGET_DATE)).isTrue
    }

    @Test
    fun `Should return false for potentialTreatmentBeforeMaxDate if both start date and stop date are after target date`() {
        val treatment = treatment(
            stopYear = RECENT_DATE.year,
            stopMonth = RECENT_DATE.monthValue,
            startYear = RECENT_DATE.year,
            startMonth = RECENT_DATE.monthValue
        )
        assertThat(TreatmentVersusDateFunctions.potentialTreatmentBeforeMaxDate(treatment, TARGET_DATE)).isFalse
    }

    @Test
    fun `Should return true for potentialTreatmentBeforeMaxDate if stop date is after target date but start date is before target date`() {
        val treatment = treatment(
            stopYear = RECENT_DATE.year,
            stopMonth = RECENT_DATE.monthValue,
            startYear = OLDER_DATE.year,
            startMonth = OLDER_DATE.monthValue
        )
        assertThat(TreatmentVersusDateFunctions.potentialTreatmentBeforeMaxDate(treatment, TARGET_DATE)).isTrue
    }

    @Test
    fun `Should return true for potentialTreatmentBeforeMaxDate if stop date is after target date and start date is unknown`() {
        val treatment = treatment(stopYear = RECENT_DATE.year, stopMonth = RECENT_DATE.monthValue)
        assertThat(TreatmentVersusDateFunctions.potentialTreatmentBeforeMaxDate(treatment, TARGET_DATE)).isTrue
    }

    private fun treatment(
        stopYear: Int? = null,
        stopMonth: Int? = null,
        startYear: Int? = null,
        startMonth: Int? = null,
        maxStopYear: Int? = null,
        maxStopMonth: Int? = null
    ): TreatmentHistoryEntry {
        return treatmentHistoryEntry(
            setOf(TREATMENT),
            startYear = startYear,
            startMonth = startMonth,
            stopYear = stopYear,
            stopMonth = stopMonth,
            maxStopYear = maxStopYear,
            maxStopMonth = maxStopMonth
        )
    }
}