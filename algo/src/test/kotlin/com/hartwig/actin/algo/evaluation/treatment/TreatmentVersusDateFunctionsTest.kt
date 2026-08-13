package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.algo.evaluation.treatment.TreatmentVersusDateFunctions.certainTreatmentSinceMinDate
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private val TARGET_DATE = LocalDate.of(2025, 6, 1)
private val RECENT_DATE = TARGET_DATE.plusYears(1)
private val OLDER_DATE = TARGET_DATE.minusYears(1)

class TreatmentVersusDateFunctionsTest {

    @Test
    fun `Should return true for certainTreatmentSinceMinDate if stop date is after target date`() {
        val treatment = treatment(stopYear = RECENT_DATE.year, stopMonth = RECENT_DATE.monthValue)
        val result = certainTreatmentSinceMinDate(treatment, TARGET_DATE)

        assertThat(result).isTrue
    }

    @Test
    fun `Should return false for certainTreatmentSinceMinDate if stop date is before target date`() {
        val treatment = treatment(stopYear = OLDER_DATE.year, stopMonth = OLDER_DATE.monthValue)
        val result = certainTreatmentSinceMinDate(treatment, TARGET_DATE)

        assertThat(result).isFalse
    }

    @Test
    fun `Should return true for certainTreatmentSinceMinDate if stop date is unknown but start date is after target date`() {
        val treatment = treatment(startYear = RECENT_DATE.year, startMonth = RECENT_DATE.monthValue)
        val result = certainTreatmentSinceMinDate(treatment, TARGET_DATE)

        assertThat(result).isTrue
    }

    @Test
    fun `Should return false for certainTreatmentSinceMinDate if stop date is unknown and start date is before target date`() {
        val treatment = treatment(startYear = OLDER_DATE.year, startMonth = OLDER_DATE.monthValue)
        val result = certainTreatmentSinceMinDate(treatment, TARGET_DATE)

        assertThat(result).isFalse
    }

    @Test
    fun `Should return false for certainTreatmentSinceMinDate if both stop date and start date are unknown`() {
        val treatment = treatment()
        val result = certainTreatmentSinceMinDate(treatment, TARGET_DATE)

        assertThat(result).isFalse
    }

    @Test
    fun `Should return true for potentialTreatmentSinceMinDate if stop date is after target date`() {
        val treatment = treatment(stopYear = RECENT_DATE.year, stopMonth = RECENT_DATE.monthValue)
        val result = TreatmentVersusDateFunctions.potentialTreatmentSinceMinDate(treatment, TARGET_DATE)

        assertThat(result).isTrue
    }

    @Test
    fun `Should return false for potentialTreatmentSinceMinDate if stop date is before target date`() {
        val treatment = treatment(stopYear = OLDER_DATE.year, stopMonth = OLDER_DATE.monthValue)
        val result = TreatmentVersusDateFunctions.potentialTreatmentSinceMinDate(treatment, TARGET_DATE)

        assertThat(result).isFalse
    }

    @Test
    fun `Should return true for potentialTreatmentSinceMinDate if stop date is unknown`() {
        val treatment = treatment()
        val result = TreatmentVersusDateFunctions.potentialTreatmentSinceMinDate(treatment, TARGET_DATE)

        assertThat(result).isTrue
    }

    @Test
    fun `Should return true for potentialTreatmentSinceMinDate if stop date is unknown but maxStopDate is after target date`() {
        val treatment = treatment(maxStopYear = RECENT_DATE.year, maxStopMonth = RECENT_DATE.monthValue)
        val result = TreatmentVersusDateFunctions.potentialTreatmentSinceMinDate(treatment, TARGET_DATE)

        assertThat(result).isTrue
    }

    @Test
    fun `Should return false for potentialTreatmentSinceMinDate if stop date is unknown but maxStopDate is before target date`() {
        val treatment = treatment(maxStopYear = OLDER_DATE.year, maxStopMonth = OLDER_DATE.monthValue)
        val result = TreatmentVersusDateFunctions.potentialTreatmentSinceMinDate(treatment, TARGET_DATE)

        assertThat(result).isFalse
    }

    @Test
    fun `Should return true for treatmentBeforeMaxDate if stop date is before target date`() {
        val treatment = treatment(stopYear = OLDER_DATE.year, stopMonth = OLDER_DATE.monthValue)
        val result = TreatmentVersusDateFunctions.treatmentBeforeMaxDate(treatment, TARGET_DATE, false)

        assertThat(result).isTrue
    }

    @Test
    fun `Should return false for treatmentBeforeMaxDate if stop date is after target date`() {
        val treatment = treatment(stopYear = RECENT_DATE.year, stopMonth = RECENT_DATE.monthValue)
        val result = TreatmentVersusDateFunctions.treatmentBeforeMaxDate(treatment, TARGET_DATE, false)

        assertThat(result).isFalse
    }

    @Test
    fun `Should return false for treatmentBeforeMaxDate if stop date is unknown and includeUnknown is false`() {
        val treatment = treatment()
        val result = TreatmentVersusDateFunctions.treatmentBeforeMaxDate(treatment, TARGET_DATE, false)

        assertThat(result).isFalse
    }

    @Test
    fun `Should return true for treatmentBeforeMaxDate if stop date is unknown and includeUnknown is true`() {
        val treatment = treatment()
        val result = TreatmentVersusDateFunctions.treatmentBeforeMaxDate(treatment, TARGET_DATE, true)

        assertThat(result).isTrue
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
            setOf(
                DrugTreatment(
                    name = "treatment",
                    drugs = setOf(Drug(name = "drug", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = emptySet()))
                )
            ),
            startYear = startYear,
            startMonth = startMonth,
            stopYear = stopYear,
            stopMonth = stopMonth,
            maxStopYear = maxStopYear,
            maxStopMonth = maxStopMonth
        )
    }
}