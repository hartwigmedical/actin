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
                ArcherFusion("RET"),
            ),
            exonSkipping = listOf(ArcherSkippedExons("MET", 1, 4))
        )

        assertThat(archerPanel.testedGenes()).isEqualTo(setOf("KRAS", "RET", "MET") + ARCHER_ALWAYS_TESTED_GENES)
    }
}