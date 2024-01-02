package com.hartwig.actin.clinical.sort

import com.hartwig.actin.clinical.datamodel.BodyWeight
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class BodyWeightDescendingDateComparatorTest {

    @Test
    fun `Should sort body weights`() {
        val weight1 = weight(LocalDate.of(2020, 4, 4), 0.0, "")
        val weight2 = weight(LocalDate.of(2020, 4, 4), 80.0, "unit 1")
        val weight3 = weight(LocalDate.of(2020, 4, 4), 80.0, "unit 2")
        val weight4 = weight(LocalDate.of(2021, 4, 4), 0.0, "")
        val weights = listOf(weight1, weight2, weight4, weight3).sortedWith(BodyWeightDescendingDateComparator())

        assertThat(weights).containsExactly(weight4, weight2, weight3, weight1)
    }

    private fun weight(date: LocalDate, value: Double, unit: String): BodyWeight {
        return BodyWeight(date = date, value = value, unit = unit)
    }
}