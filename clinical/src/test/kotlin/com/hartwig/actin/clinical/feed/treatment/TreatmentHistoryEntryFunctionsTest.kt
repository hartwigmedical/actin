package com.hartwig.actin.clinical.feed.treatment

import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private val MAX_DATE = LocalDate.of(2026, 1, 1)

class TreatmentHistoryEntryFunctionsTest {

    @Test
    fun `Should set stop date for systemic treatment with no stop year and month followed by systemic treatment`() {
        val treatmentHistoryEntries = listOf(
            createChemotherapy(2020, 6, null),
            createChemotherapy(2020, 9, null)
        )
        val output = TreatmentHistoryEntryFunctions.setMaxStopDate(treatmentHistoryEntries, MAX_DATE)
        assertThat(output[0].treatmentHistoryDetails?.maxStopYear).isEqualTo(2020)
        assertThat(output[0].treatmentHistoryDetails?.maxStopMonth).isEqualTo(9)
        assertThat(output[1].treatmentHistoryDetails?.maxStopYear).isEqualTo(MAX_DATE.year)
        assertThat(output[1].treatmentHistoryDetails?.maxStopMonth).isEqualTo(MAX_DATE.monthValue + 1)
    }

    @Test
    fun `Should not set stop date for systemic treatment with no stop year and month followed by non-systemic treatments`() {
        val treatmentHistoryEntries = listOf(
            createChemotherapy(2020, 6, null),
            treatmentHistoryEntry(
                treatments = setOf(
                    TreatmentTestFactory.treatment(
                        "radiotherapy",
                        false,
                        setOf(TreatmentCategory.RADIOTHERAPY)
                    )
                ), startYear = 2020, startMonth = 7
            )
        )
        val output = TreatmentHistoryEntryFunctions.setMaxStopDate(treatmentHistoryEntries, MAX_DATE)
        assertThat(output[0].treatmentHistoryDetails?.maxStopYear).isEqualTo(MAX_DATE.year)
        assertThat(output[0].treatmentHistoryDetails?.maxStopMonth).isEqualTo(MAX_DATE.monthValue + 1)
    }

    @Test
    fun `Should set stop date for systemic treatment with no stop year and month followed by systemic treatment combined with non-systemic treatment`() {
        val treatmentHistoryEntries = listOf(
            createChemotherapy(2021, 6, null),
            treatmentHistoryEntry(
                treatments = setOf(
                    TreatmentTestFactory.treatment(
                        "radiotherapy",
                        false,
                        setOf(TreatmentCategory.RADIOTHERAPY)
                    ),
                    TreatmentTestFactory.treatment(
                        "chemotherapy",
                        true,
                        setOf(TreatmentCategory.CHEMOTHERAPY)
                    )
                ), startYear = 2021, startMonth = 7
            )
        )
        val output = TreatmentHistoryEntryFunctions.setMaxStopDate(treatmentHistoryEntries, MAX_DATE)
        assertThat(output[0].treatmentHistoryDetails?.maxStopYear).isEqualTo(2021)
        assertThat(output[0].treatmentHistoryDetails?.maxStopMonth).isEqualTo(7)
    }

    @Test
    fun `Should set stop date for systemic treatment with stop year and no stop month followed by systemic treatment in other year`() {
        val treatmentHistoryEntries = listOf(
            createChemotherapy(2020, null, 2021),
            createChemotherapy(2022, null, 2023)
        )
        val output = TreatmentHistoryEntryFunctions.setMaxStopDate(treatmentHistoryEntries, MAX_DATE)
        assertThat(output[0].treatmentHistoryDetails?.maxStopYear).isNull()
        assertThat(output[0].treatmentHistoryDetails?.maxStopMonth).isEqualTo(12)
        assertThat(output[1].treatmentHistoryDetails?.maxStopYear).isNull()
        assertThat(output[1].treatmentHistoryDetails?.maxStopMonth).isEqualTo(12)
    }

    @Test
    fun `Should set stop date for systemic treatment with stop year and no stop month followed by non-systemic treatment`() {
        val treatmentHistoryEntries = listOf(
            createChemotherapy(2020, 6, 2021),
            treatmentHistoryEntry(
                treatments = setOf(
                    TreatmentTestFactory.treatment(
                        "radiotherapy",
                        false,
                        setOf(TreatmentCategory.RADIOTHERAPY)
                    )
                ), startYear = 2020, startMonth = 7
            )
        )
        val output = TreatmentHistoryEntryFunctions.setMaxStopDate(treatmentHistoryEntries, MAX_DATE)
        assertThat(output[0].treatmentHistoryDetails?.maxStopYear).isNull()
        assertThat(output[0].treatmentHistoryDetails?.maxStopMonth).isEqualTo(12)
        assertThat(output[1].treatmentHistoryDetails?.maxStopYear).isNull()
        assertThat(output[1].treatmentHistoryDetails?.maxStopMonth).isNull()
    }

    @Test
    fun `Should set stop date for systemic treatment with stop year and no stop month followed by systemic treatment in same year`() {
        val treatmentHistoryEntries = listOf(
            createChemotherapy(2020, null, 2021),
            createChemotherapy(2021, null, 2022)
        )
        val output = TreatmentHistoryEntryFunctions.setMaxStopDate(treatmentHistoryEntries, MAX_DATE)
        assertThat(output[0].treatmentHistoryDetails?.maxStopYear).isNull()
        assertThat(output[0].treatmentHistoryDetails?.maxStopMonth).isEqualTo(12)
        assertThat(output[1].treatmentHistoryDetails?.maxStopYear).isNull()
        assertThat(output[1].treatmentHistoryDetails?.maxStopMonth).isEqualTo(12)
    }

    private fun createChemotherapy(startYear: Int, startMonth: Int?, stopYear: Int?): TreatmentHistoryEntry {
        return treatmentHistoryEntry(
            treatments = setOf(TreatmentTestFactory.treatment("chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY))),
            startYear = startYear,
            startMonth = startMonth,
            stopYear = stopYear
        )
    }
}