package com.hartwig.actin.molecular.datamodel.panel.archer

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ArcherPanelTest {

    @Test
    fun `Should identify tested genes`() {
        val archerPanel = ArcherPanel(
            variants = listOf(
                ArcherVariant("KRAS", "c.1A>T"),
            ),
            fusions = listOf(
                ArcherFusion("RET", "MET"),
            )
        )

        assertThat(archerPanel.testedGenes()).containsExactlyInAnyOrder("ALK", "ROS1", "RET", "MET", "NTRK", "NRG1", "KRAS")
    }
}