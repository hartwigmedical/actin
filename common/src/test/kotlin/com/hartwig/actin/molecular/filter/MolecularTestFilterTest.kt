package com.hartwig.actin.molecular.filter

import com.hartwig.actin.datamodel.molecular.Drivers
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.Fusion
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.TestPanelRecordFactory
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val BASE_PANEL_TEST = TestPanelRecordFactory.empty()
private val BASE_WGS_TEST = TestMolecularFactory.createMinimalTestMolecularRecord()
private val ONE_YEAR_AGO = LocalDate.of(2023, 9, 9)

class MolecularTestFilterTest {

    private val filter = MolecularTestFilter(ONE_YEAR_AGO)

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
        val oldPanel = BASE_PANEL_TEST.copy(date = ONE_YEAR_AGO.plusDays(1), drivers = Drivers(fusions = setOf(fusion)))
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
        val oldPanel = BASE_PANEL_TEST.copy(date = ONE_YEAR_AGO.minusDays(1), drivers = Drivers(fusions = setOf(fusion)))
        val newOncoPanel = BASE_WGS_TEST.copy(date = ONE_YEAR_AGO.plusDays(2), experimentType = ExperimentType.HARTWIG_TARGETED)
        testFilter(newOncoPanel, oldPanel)
    }

    @Test
    fun `Should filter out somatic information from failed WGS record`() {
        val failedWGS = BASE_WGS_TEST.copy(hasSufficientQuality = false)
        val filtered = filter.apply(listOf(failedWGS)).first()
        assertThat(filtered.drivers).isEqualTo(Drivers())
        assertThat((filtered as MolecularRecord).pharmaco).isEqualTo(failedWGS.pharmaco)
    }

    private fun testFilter(toInclude: MolecularTest, toFilter: MolecularTest) {
        val filtered = filter.apply(listOf(toInclude, toFilter))
        assertThat(filtered).containsOnly(toInclude)
    }
}