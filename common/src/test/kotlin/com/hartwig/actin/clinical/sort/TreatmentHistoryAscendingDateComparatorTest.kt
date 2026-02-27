package com.hartwig.actin.clinical.sort

import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatment
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentHistoryAscendingDateComparatorTest {

    @Test
    fun `Should sort by ascending start date then by ascending end date then by name`() {
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

        val treatments = listOf(
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
        ).sortedWith(TreatmentHistoryAscendingDateComparator())

        assertThat(treatments).containsExactly(
            treatment1,
            treatment2,
            treatment3,
            treatment4,
            treatment5,
            treatment6,
            treatment7,
            treatment8,
            treatment9,
            treatment10,
            treatment11
        )
    }

    private fun create(name: String, startYear: Int?, startMonth: Int?, stopYear: Int?, stopMonth: Int?): TreatmentHistoryEntry {
        return TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(OtherTreatment(name = name, isSystemic = false, categories = emptySet())),
            startYear = startYear,
            startMonth = startMonth,
            stopYear = stopYear,
            stopMonth = stopMonth
        )
    }
}