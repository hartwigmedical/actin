package com.hartwig.actin.molecular.filter

import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private val BASE_PANEL_TEST = TestMolecularFactory.createMinimalTestPanelRecord()
private val BASE_WGS_TEST = TestMolecularFactory.createMinimalTestMolecularRecord()
private val ONE_YEAR_AGO = LocalDate.of(2023, 9, 9)

class MolecularTestFilterTest {

    private val filter = MolecularTestFilter(ONE_YEAR_AGO, true)

    @Test
    fun `Should filter tests both older than max age date when more recent data is available`() {
        val recent = BASE_PANEL_TEST.copy(date = ONE_YEAR_AGO.plusDays(1))
        val old = BASE_PANEL_TEST.copy(date = ONE_YEAR_AGO.minusDays(1))
        testFilter(recent, old)
    }

    @Test
    fun `Should not filter tests that are older than max age, but no more recent data is available`() {
        val old = BASE_PANEL_TEST.copy(date = ONE_YEAR_AGO.minusDays(1))
        val older = BASE_PANEL_TEST.copy(date = ONE_YEAR_AGO.minusDays(2))
        testFilter(old, older)
    }

    @Test
    fun `Should filter out panel data older than the most recent OncoAct`() {
        val oldPanel = BASE_PANEL_TEST.copy(date = ONE_YEAR_AGO.plusDays(1))
        val newWGS = BASE_WGS_TEST.copy(date = ONE_YEAR_AGO.plusDays(2))
        testFilter(newWGS, oldPanel)
    }

    @Test
    fun `Should not filter out panel data older than the most recent OncoPanel but with fusion data`() {
        val fusion = mockk<Fusion>()
        val oldPanel = BASE_PANEL_TEST.copy(
            date = ONE_YEAR_AGO.plusDays(1),
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(fusions = listOf(fusion))
        )
        val newOncoPanel = BASE_WGS_TEST.copy(date = ONE_YEAR_AGO.plusDays(2), experimentType = ExperimentType.HARTWIG_TARGETED)
        val filtered = filter.apply(listOf(oldPanel, newOncoPanel))
        assertThat(filtered).containsExactlyInAnyOrder(oldPanel, newOncoPanel)
    }

    @Test
    fun `Should filter out panel data older than the most recent OncoPanel but with no fusion data`() {
        val oldPanel = BASE_PANEL_TEST.copy(date = ONE_YEAR_AGO.plusDays(1))
        val newOncoPanel = BASE_WGS_TEST.copy(date = ONE_YEAR_AGO.plusDays(2), experimentType = ExperimentType.HARTWIG_TARGETED)
        testFilter(newOncoPanel, oldPanel)
    }

    @Test
    fun `Should filter out panel data older than the most recent OncoPanel but with fusion data but older than one year`() {
        val fusion = mockk<Fusion>()
        val oldPanel = BASE_PANEL_TEST.copy(
            date = ONE_YEAR_AGO.minusDays(1),
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(fusions = listOf(fusion))
        )
        val newOncoPanel = BASE_WGS_TEST.copy(date = ONE_YEAR_AGO.plusDays(2), experimentType = ExperimentType.HARTWIG_TARGETED)
        testFilter(newOncoPanel, oldPanel)
    }

    @Test
    fun `Should filter out records with insufficient quality if useInsufficientQualityRecords is false`() {
        val filterInsufficientQuality = MolecularTestFilter(ONE_YEAR_AGO, false)
        val filtered = filterInsufficientQuality.apply(listOf(BASE_WGS_TEST, BASE_WGS_TEST.copy(hasSufficientQuality = false)))
        assertThat(filtered).containsOnly(BASE_WGS_TEST)
    }

    private fun testFilter(toInclude: MolecularTest, toFilter: MolecularTest) {
        val filtered = filter.apply(listOf(toInclude, toFilter))
        assertThat(filtered).containsOnly(toInclude)
    }
}