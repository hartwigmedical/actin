package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class TreatmentHistoryEntryStartDateComparatorTest {

    @Test
    fun `Should sort correctly based on start year`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(setOf(drugTreatment("Test treatment 1", TreatmentCategory.CHEMOTHERAPY)), 2021, 5),
            treatmentHistoryEntry(setOf(drugTreatment("Test treatment 2", TreatmentCategory.CHEMOTHERAPY)), 2020, 8)
        )

        assertEquals("Test treatment 1", treatmentHistory.maxWith(TreatmentHistoryEntryStartDateComparator()).treatmentName())
    }

    @Test
    fun `Should sort correctly based on start month`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(setOf(drugTreatment("Test treatment 1", TreatmentCategory.CHEMOTHERAPY)), 2021, 5),
            treatmentHistoryEntry(setOf(drugTreatment("Test treatment 2", TreatmentCategory.CHEMOTHERAPY)), 2021, 4)
        )

        assertEquals("Test treatment 1", treatmentHistory.maxWith(TreatmentHistoryEntryStartDateComparator()).treatmentName())
    }

    @Test
    fun `Should interpret specific year as newer than unknown year`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(setOf(drugTreatment("Test treatment 1", TreatmentCategory.CHEMOTHERAPY)), null, 6),
            treatmentHistoryEntry(setOf(drugTreatment("Test treatment 2", TreatmentCategory.CHEMOTHERAPY)), 2021, 5)
        )

        assertEquals("Test treatment 2", treatmentHistory.maxWith(TreatmentHistoryEntryStartDateComparator()).treatmentName())
    }

    @Test
    fun `Should interpret specific month as newer than unknown month`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(setOf(drugTreatment("Test treatment 1", TreatmentCategory.CHEMOTHERAPY)), 2021, null),
            treatmentHistoryEntry(setOf(drugTreatment("Test treatment 2", TreatmentCategory.CHEMOTHERAPY)), 2021, 5)
        )

        assertEquals("Test treatment 2", treatmentHistory.maxWith(TreatmentHistoryEntryStartDateComparator()).treatmentName())
    }

    @Test
    fun `Should not change order if start year and month are equal`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(setOf(drugTreatment("Test treatment 1", TreatmentCategory.CHEMOTHERAPY)), 2021, 5),
            treatmentHistoryEntry(setOf(drugTreatment("Test treatment 2", TreatmentCategory.CHEMOTHERAPY)), 2021, 5)
        )

        assertEquals("Test treatment 1", treatmentHistory.maxWith(TreatmentHistoryEntryStartDateComparator()).treatmentName())
    }
}