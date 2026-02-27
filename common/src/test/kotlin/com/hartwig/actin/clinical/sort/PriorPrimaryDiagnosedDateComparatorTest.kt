package com.hartwig.actin.clinical.sort

import com.hartwig.actin.datamodel.clinical.PriorPrimary
import com.hartwig.actin.datamodel.clinical.TestPriorPrimaryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PriorPrimaryDiagnosedDateComparatorTest {

    @Test
    fun `Should sort on diagnosed year then diagnosed month nulls last`() {
        val secondPrimary1 = withYearMonth(2022, 2)
        val secondPrimary2 = withYearMonth(2022, 5)
        val secondPrimary3 = withYearMonth(2022, null)
        val secondPrimary4 = withYearMonth(2023, 1)
        val secondPrimary5 = withYearMonth(null, null)
        val sorted = listOf(secondPrimary2, secondPrimary3, secondPrimary5, secondPrimary4, secondPrimary1)
            .sortedWith(PriorPrimaryDiagnosedDateComparator())

        assertThat(sorted).containsExactly(secondPrimary1, secondPrimary2, secondPrimary3, secondPrimary4, secondPrimary5)
    }

    private fun withYearMonth(diagnosedYear: Int?, diagnosedMonth: Int?): PriorPrimary {
        return TestPriorPrimaryFactory.createMinimal().copy(diagnosedYear = diagnosedYear, diagnosedMonth = diagnosedMonth)
    }
}