package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentHistoryEntryStartDateComparatorTest {

    @Test
    fun `Should sort correctly based on start year`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(setOf(drugTreatment("Test treatment 1", TreatmentCategory.CHEMOTHERAPY)), 2021, 5),
            treatmentHistoryEntry(setOf(drugTreatment("Test treatment 2", TreatmentCategory.CHEMOTHERAPY)), 2020, 8)
        )

        assertThat(treatmentHistory.maxWith(TreatmentHistoryEntryStartDateComparator()).treatmentName()).isEqualTo("Test treatment 1")
    }

    @Test
    fun `Should sort correctly based on start month`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(setOf(drugTreatment("Test treatment 1", TreatmentCategory.CHEMOTHERAPY)), 2021, 5),
            treatmentHistoryEntry(setOf(drugTreatment("Test treatment 2", TreatmentCategory.CHEMOTHERAPY)), 2021, 4)
        )

        assertThat(treatmentHistory.maxWith(TreatmentHistoryEntryStartDateComparator()).treatmentName()).isEqualTo("Test treatment 1")
    }

    @Test
    fun `Should interpret specific year as newer than unknown year`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(setOf(drugTreatment("Test treatment 1", TreatmentCategory.CHEMOTHERAPY)), null, 6),
            treatmentHistoryEntry(setOf(drugTreatment("Test treatment 2", TreatmentCategory.CHEMOTHERAPY)), 2021, 5)
        )

        assertThat(treatmentHistory.maxWith(TreatmentHistoryEntryStartDateComparator()).treatmentName()).isEqualTo("Test treatment 2")
    }

    @Test
    fun `Should interpret specific month as newer than unknown month`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(setOf(drugTreatment("Test treatment 1", TreatmentCategory.CHEMOTHERAPY)), 2021, null),
            treatmentHistoryEntry(setOf(drugTreatment("Test treatment 2", TreatmentCategory.CHEMOTHERAPY)), 2021, 5)
        )

        assertThat(treatmentHistory.maxWith(TreatmentHistoryEntryStartDateComparator()).treatmentName()).isEqualTo("Test treatment 2")
    }

    @Test
    fun `Should not change order if start year and month are equal`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(setOf(drugTreatment("Test treatment 1", TreatmentCategory.CHEMOTHERAPY)), 2021, 5),
            treatmentHistoryEntry(setOf(drugTreatment("Test treatment 2", TreatmentCategory.CHEMOTHERAPY)), 2021, 5)
        )

        assertThat(treatmentHistory.maxWith(TreatmentHistoryEntryStartDateComparator()).treatmentName()).isEqualTo("Test treatment 1")
    }
}