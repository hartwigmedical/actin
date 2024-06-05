package com.hartwig.actin.molecular.datamodel.panel.archer

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ArcherPanelExtractionTest {

    @Test
    fun `Should identify tested genes`() {
        val archerPanel = ArcherPanelExtraction(
            variants = listOf(ArcherVariantExtraction("KRAS", "c.1A>T")),
            fusions = listOf(
                ArcherFusionExtraction("RET"),
            ),
            skippedExons = listOf(ArcherSkippedExonsExtraction("MET", 1, 4))
        )

        assertThat(archerPanel.testedGenes()).isEqualTo(setOf("KRAS", "RET", "MET") + ARCHER_ALWAYS_TESTED_GENES)
    }
}