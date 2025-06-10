package com.hartwig.actin.clinical.feed.treatment

import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentHistoryEntryFunctionsTest {

    @Test
    fun `Should set stop date for systemic treatment followed by systemic treatment`() {
        val treatmentHistoryEntries = listOf(createChemotherapy(2020, 6), createChemotherapy(2020, 9))
        val output = TreatmentHistoryEntryFunctions.setMaxStopDate(treatmentHistoryEntries)
        assertThat(output[0].treatmentHistoryDetails?.maxStopYear).isEqualTo(2020)
        assertThat(output[0].treatmentHistoryDetails?.maxStopMonth).isEqualTo(9)
        assertThat(output[1].treatmentHistoryDetails?.maxStopYear).isNull()
        assertThat(output[1].treatmentHistoryDetails?.maxStopMonth).isNull()
    }

    @Test
    fun `Should not set stop date for systemic treatment followed by non-systemic treatments`() {
        val treatmentHistoryEntries = listOf(
            createChemotherapy(2020, 6),
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
        val output = TreatmentHistoryEntryFunctions.setMaxStopDate(treatmentHistoryEntries)
        assertThat(output[0].treatmentHistoryDetails?.maxStopYear).isNull()
        assertThat(output[0].treatmentHistoryDetails?.maxStopMonth).isNull()
    }

    @Test
    fun `Should not set stop date for systemic treatment followed by systemic treatment combined with non-systemic treatment`() {
        val treatmentHistoryEntries = listOf(
            createChemotherapy(2020, 6),
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
                ), startYear = 2020, startMonth = 7
            )
        )
        val output = TreatmentHistoryEntryFunctions.setMaxStopDate(treatmentHistoryEntries)
        assertThat(output[0].treatmentHistoryDetails?.maxStopYear).isNull()
        assertThat(output[0].treatmentHistoryDetails?.maxStopMonth).isNull()
    }

    private fun createChemotherapy(startYear: Int, startMonth: Int): TreatmentHistoryEntry {
        return treatmentHistoryEntry(
            treatments = setOf(TreatmentTestFactory.treatment("chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY))),
            startYear = startYear,
            startMonth = startMonth
        )
    }
}