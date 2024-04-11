package com.hartwig.actin.molecular.datamodel.archer

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class ArcherPanelTest {

    @Test
    fun `Should identify genes for which no impact was detected`() {
        val archerPanel = ArcherPanel(
            date = LocalDate.of(2021, 1, 1),
            variants = listOf(
                Variant("ALK", "c.1A>T"),
            ),
            fusions = listOf(
                Fusion("RET", "MET"),
            )
        )

        assertThat(archerPanel.genesWithNoImpact()).containsExactlyInAnyOrder("ROS1", "NTRK", "NRG1")
    }
}