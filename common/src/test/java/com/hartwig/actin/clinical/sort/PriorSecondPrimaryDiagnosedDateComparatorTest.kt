package com.hartwig.actin.clinical.sort

import com.google.common.collect.Lists
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.TestPriorSecondPrimaryFactory
import org.assertj.core.api.Assertions
import org.junit.Test

class PriorSecondPrimaryDiagnosedDateComparatorTest {
    @Test
    fun shouldSortOnDiagnosedYearThenDiagnosedMonthNullsLast() {
        val secondPrimary1 = withYearMonth(2022, 2)
        val secondPrimary2 = withYearMonth(2022, 5)
        val secondPrimary3 = withYearMonth(2022, null)
        val secondPrimary4 = withYearMonth(2023, 1)
        val secondPrimary5 = withYearMonth(null, null)
        val sorted: List<PriorSecondPrimary> =
            Lists.newArrayList(secondPrimary2, secondPrimary3, secondPrimary5, secondPrimary4, secondPrimary1)
        sorted.sort(PriorSecondPrimaryDiagnosedDateComparator())
        Assertions.assertThat(sorted[0]).isEqualTo(secondPrimary1)
        Assertions.assertThat(sorted[1]).isEqualTo(secondPrimary2)
        Assertions.assertThat(sorted[2]).isEqualTo(secondPrimary3)
        Assertions.assertThat(sorted[3]).isEqualTo(secondPrimary4)
        Assertions.assertThat(sorted[4]).isEqualTo(secondPrimary5)
    }

    companion object {
        private fun withYearMonth(diagnosedYear: Int?, diagnosedMonth: Int?): PriorSecondPrimary {
            return TestPriorSecondPrimaryFactory.builder().diagnosedYear(diagnosedYear).diagnosedMonth(diagnosedMonth).build()
        }
    }
}