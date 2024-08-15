package com.hartwig.actin.molecular.datamodel.panel.archer

import com.hartwig.actin.molecular.datamodel.panel.PanelFusionExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelSkippedExonsExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelVariantExtraction
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ArcherPanelExtractionTest {

    @Test
    fun `Should identify tested genes`() {
        val archerPanel = ArcherPanelExtraction(
            variants = listOf(PanelVariantExtraction("KRAS", "c.1A>T")),
            fusions = listOf(PanelFusionExtraction("RET", null)),
            skippedExons = listOf(PanelSkippedExonsExtraction("MET", 1, 4, null))
        )

        assertThat(archerPanel.testedGenes()).isEqualTo(setOf("KRAS", "RET", "MET") + ARCHER_ALWAYS_TESTED_GENES)
    }
}