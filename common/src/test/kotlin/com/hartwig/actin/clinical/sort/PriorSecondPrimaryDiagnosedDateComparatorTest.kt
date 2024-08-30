package com.hartwig.actin.clinical.sort

import com.hartwig.actin.datamodel.clinical.PriorSecondPrimary
import com.hartwig.actin.datamodel.clinical.TestPriorSecondPrimaryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PriorSecondPrimaryDiagnosedDateComparatorTest {

    @Test
    fun `Should sort on diagnosed year then diagnosed month nulls last`() {
        val secondPrimary1 = withYearMonth(2022, 2)
        val secondPrimary2 = withYearMonth(2022, 5)
        val secondPrimary3 = withYearMonth(2022, null)
        val secondPrimary4 = withYearMonth(2023, 1)
        val secondPrimary5 = withYearMonth(null, null)
        val sorted = listOf(secondPrimary2, secondPrimary3, secondPrimary5, secondPrimary4, secondPrimary1)
            .sortedWith(PriorSecondPrimaryDiagnosedDateComparator())

        assertThat(sorted).containsExactly(secondPrimary1, secondPrimary2, secondPrimary3, secondPrimary4, secondPrimary5)
    }

    private fun withYearMonth(diagnosedYear: Int?, diagnosedMonth: Int?): PriorSecondPrimary {
        return TestPriorSecondPrimaryFactory.createMinimal().copy(diagnosedYear = diagnosedYear, diagnosedMonth = diagnosedMonth)
    }
}