package com.hartwig.actin.clinical.feed.treatment

import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentHistoryEntryFunctionsTest {

    @Test
    fun `Should set stop date`() {
        val test = listOf(
            treatmentHistoryEntry(
                treatments = setOf(
                    TreatmentTestFactory.treatment(
                        "",
                        true,
                        setOf(TreatmentCategory.CHEMOTHERAPY)
                    )
                ), startYear = 2020, startMonth = 6
            ),
            treatmentHistoryEntry(
                treatments = setOf(
                    TreatmentTestFactory.treatment(
                        "",
                        true,
                        setOf(TreatmentCategory.RADIOTHERAPY)
                    )
                ), startYear = 2020, startMonth = 7
            ),
            treatmentHistoryEntry(
                treatments = setOf(TreatmentTestFactory.treatment("", true, setOf(TreatmentCategory.CHEMOTHERAPY))),
                startYear = 2020,
                startMonth = 9
            )
        )
        val output = TreatmentHistoryEntryFunctions.setMaxStopDate(test)
        assertThat(output[0].treatmentHistoryDetails?.maxStopYear).isEqualTo(2020)
        assertThat(output[0].treatmentHistoryDetails?.maxStopMonth).isEqualTo(9)
        assertThat(output[1].treatmentHistoryDetails?.maxStopYear).isNull()
        assertThat(output[1].treatmentHistoryDetails?.maxStopMonth).isNull()
    }
}