package com.hartwig.actin.algo.molecular

import com.hartwig.actin.algo.evaluation.molecular.IhcTestFilter
import com.hartwig.actin.algo.evaluation.molecular.IhcTestFilter.allPDL1TestsByMeasureToFind
import com.hartwig.actin.algo.evaluation.molecular.IhcTestFilter.mostRecentAndUnknownDateIhcTestsForItem
import com.hartwig.actin.datamodel.clinical.IhcTest
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IhcTestFilterTest {

    @Test
    fun `Should remove all entries for PDL1 if measure to find is null`() {
        val test1 = ihcTest(item = "PD-L1")
        val test2 = ihcTest(item = "PD-L1", measure = "TPS")
        val test3 = ihcTest(item = "BRAF")
        val filtered = allPDL1TestsByMeasureToFind(listOf(test1, test2, test3), null)
        assertThat(filtered).isEmpty()
    }

    @Test
    fun `Should filter prior ihc tests for PDL1 with specific measure`() {
        val test1 = ihcTest(item = "PD-L1", measure = "CPS")
        val test2 = ihcTest(item = "PD-L1", measure = "wrong")
        val test3 = ihcTest(item = "BRAF")
        val filtered = allPDL1TestsByMeasureToFind(listOf(test1, test2, test3), "CPS")
        assertThat(filtered).containsExactly(test1)
    }

    @Test
    fun `Should assume that measurement is TPS if cancer is (subtype of) lung cancer and measurement is not specified`() {
        val test2 = ihcTest(item = "PD-L1", measure = null)
        val filtered = allPDL1TestsByMeasureToFind(listOf(test2), "TPS", true)
        assertThat(filtered).containsExactly(test2)
    }

    @Test
    fun `Should filter prior ihs tests on IHC for specific protein`() {
        val test1 = ihcTest(item = "PROTEIN 1")
        val test2 = ihcTest(item = "protein 2")
        val filtered = mostRecentAndUnknownDateIhcTestsForItem(listOf(test1, test2), "protein 1")
        assertThat(filtered).containsExactly(test1)
    }

    @Test
    fun `Should filter most recent and tests without dates for each protein`() {
        val test1 = ihcTest(item = "protein 1", measureDate = null)
        val test2 = test1.copy(measureDate = LocalDate.of(2025, 2, 11))
        val test3 = test1.copy(measureDate = LocalDate.of(2024, 2, 11))
        val test4 = ihcTest(item = "protein 2", measureDate = LocalDate.of(2023, 2, 11))
        assertThat(IhcTestFilter.mostRecentAndUnknownDateIhcTests(listOf(test1, test2, test3, test4)))
            .containsOnly(test1, test2, test4)
    }

    @Test
    fun `Should return all tests of most recent date except if tests are completely identical`() {
        val test1 = ihcTest(item = "protein 1", measureDate = LocalDate.of(2024, 2, 2))
        val test2 = test1.copy()
        val test3 = test1.copy(scoreValue = 2.0)
        assertThat(IhcTestFilter.mostRecentAndUnknownDateIhcTests(listOf(test1, test2, test3))).containsOnly(test1, test3)
    }

    @Test
    fun `Should return all tests of most recent date except per report hash`() {
        val test1 = ihcTest(item = "PD-L1", measureDate = LocalDate.of(2024, 2, 2), reportHash = "reportHash1")
        val test2 = test1.copy(measureDate = LocalDate.of(2024, 2, 3), reportHash = "reportHash2")
        assertThat(IhcTestFilter.mostRecentAndUnknownDateIhcTests(listOf(test1, test2))).containsOnly(test1, test2)
    }

    private fun ihcTest(item: String = "", measure: String? = null, measureDate: LocalDate? = null, reportHash: String? = null): IhcTest {
        return IhcTest(item = item, measure = measure, measureDate = measureDate, reportHash = reportHash)
    }
}