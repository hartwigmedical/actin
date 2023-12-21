package com.hartwig.actin.clinical.sort

import com.google.common.collect.Lists
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTreatment
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import org.junit.Assert
import org.junit.Test

class TreatmentHistoryAscendingDateComparatorTest {
    @Test
    fun shouldSortByAscendingStartDateThenByAscendingEndDateThenByName() {
        val treatment1 = create("treatment A", null, null, 2018, null)
        val treatment2 = create("treatment A", 2018, null, null, null)
        val treatment3 = create("treatment A", null, null, 2020, 1)
        val treatment4 = create("treatment A", 2020, 2, null, null)
        val treatment5 = create("treatment A", 2020, 3, 2021, 6)
        val treatment6 = create("treatment B", 2020, 3, 2021, 6)
        val treatment7 = create("treatment A", 2020, 3, 2021, null)
        val treatment8 = create("treatment A", 2020, 3, null, null)
        val treatment9 = create("treatment A", null, null, 2020, 4)
        val treatment10 = create("treatment A", 2021, 1, null, null)
        val treatment11 = create("treatment A", null, null, null, null)
        val treatments: List<TreatmentHistoryEntry> = Lists.newArrayList(
            treatment11,
            treatment10,
            treatment9,
            treatment8,
            treatment7,
            treatment6,
            treatment5,
            treatment4,
            treatment3,
            treatment2,
            treatment1
        )
        treatments.sort(TreatmentHistoryAscendingDateComparator())
        Assert.assertEquals(treatment1, treatments[0])
        Assert.assertEquals(treatment2, treatments[1])
        Assert.assertEquals(treatment3, treatments[2])
        Assert.assertEquals(treatment4, treatments[3])
        Assert.assertEquals(treatment5, treatments[4])
        Assert.assertEquals(treatment6, treatments[5])
        Assert.assertEquals(treatment7, treatments[6])
        Assert.assertEquals(treatment8, treatments[7])
        Assert.assertEquals(treatment9, treatments[8])
        Assert.assertEquals(treatment10, treatments[9])
        Assert.assertEquals(treatment11, treatments[10])
    }

    companion object {
        private fun create(
            name: String, startYear: Int?, startMonth: Int?,
            stopYear: Int?, stopMonth: Int?
        ): TreatmentHistoryEntry {
            return ImmutableTreatmentHistoryEntry.builder()
                .addTreatments(ImmutableOtherTreatment.builder().name(name).isSystemic(false).build())
                .startYear(startYear)
                .startMonth(startMonth)
                .treatmentHistoryDetails(ImmutableTreatmentHistoryDetails.builder().stopYear(stopYear).stopMonth(stopMonth).build())
                .build()
        }
    }
}