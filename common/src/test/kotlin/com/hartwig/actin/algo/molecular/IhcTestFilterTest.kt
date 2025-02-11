package com.hartwig.actin.algo.molecular

import com.hartwig.actin.algo.evaluation.molecular.IhcTestFilter
import com.hartwig.actin.algo.evaluation.molecular.IhcTestFilter.allIHCTestsForProtein
import com.hartwig.actin.algo.evaluation.molecular.IhcTestFilter.allPDL1Tests
import com.hartwig.actin.datamodel.clinical.PriorIHCTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class IhcTestFilterTest {

    @Test
    fun `Should filter prior molecular tests for PDL1`() {
        val test1 = priorIHCTest(test = "Archer", item = "PD-L1")
        val test2 = priorIHCTest(test = "IHC", item = "PD-L1")
        val test3 = priorIHCTest(test = "IHC", item = "PD-L1")
        val test4 = priorIHCTest(test = "IHC", item = "BRAF")
        val filtered = allPDL1Tests(listOf(test1, test2, test3, test4))
        assertThat(filtered).containsExactlyElementsOf(listOf(test2, test3))
    }

    @Test
    fun `Should filter prior molecular tests for PDL1 with specific measure`() {
        val test1 = priorIHCTest(test = "Archer", item = "PD-L1")
        val test2 = priorIHCTest(test = "IHC", item = "PD-L1", measure = "CPS")
        val test3 = priorIHCTest(test = "IHC", item = "PD-L1", measure = "wrong")
        val test4 = priorIHCTest(test = "IHC", item = "BRAF")
        val filtered = allPDL1Tests(listOf(test1, test2, test3, test4), "CPS")
        assertThat(filtered).containsExactly(test2)
    }

    @Test
    fun `Should assume that measurement is TPS if cancer is (subtype of) lung cancer and measurement is not specified`() {
        val test1 = priorIHCTest(test = "Archer", item = "PD-L1", measure = null)
        val test2 = priorIHCTest(test = "IHC", item = "PD-L1", measure = null)
        val filtered = allPDL1Tests(listOf(test1, test2), "TPS", true)
        assertThat(filtered).containsExactly(test2)
    }

    @Test
    fun `Should filter prior molecular tests on IHC for specific protein`() {
        val test1 = priorIHCTest(test = "Archer", item = "protein 1")
        val test2 = priorIHCTest(test = "IHC", item = "protein 1")
        val test3 = priorIHCTest(test = "IHC", item = "protein 2")
        val filtered = allIHCTestsForProtein(listOf(test1, test2, test3), "protein 1")
        assertThat(filtered).containsExactly(test2)
    }

    @Test
    fun `Should filter most recent and tests without dates for each protein`() {
        val test1 = priorIHCTest(test = "IHC", item = "protein 1", measureDate = null)
        val test2 = test1.copy(measureDate = LocalDate.of(2025, 2, 11))
        val test3 = test1.copy(measureDate = LocalDate.of(2024, 2, 11))
        val test4 = priorIHCTest(test = "IHC", item = "protein 2", measureDate = LocalDate.of(2023, 2, 11))
        assertThat(IhcTestFilter.mostRecentOrUnknownDateIhcTests(listOf(test1, test2, test3, test4)))
            .containsOnly(test1, test2, test4)
    }

    @Test
    fun `Should return all tests of most recent date except if tests are completely equal`() {
        val test1 = priorIHCTest(test = "IHC", item = "protein 1", measureDate = LocalDate.of(2024, 2, 2))
        val test2 = test1.copy()
        val test3 = test1.copy(scoreValue = 2.0)
        assertThat(IhcTestFilter.mostRecentOrUnknownDateIhcTests(listOf(test1, test2, test3))).containsOnly(test1, test3)
    }

    private fun priorIHCTest(test: String = "", item: String = "", measure: String? = null, measureDate: LocalDate? = null): PriorIHCTest {
        return PriorIHCTest(test = test, item = item, measure = measure, measureDate = measureDate)
    }
}