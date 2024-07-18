package com.hartwig.actin.molecular.datamodel.panel.generic

import com.hartwig.actin.molecular.datamodel.AVL_PANEL
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GenericPanelExtractionTestRecord {

    @Test
    fun `Should identify tested genes`() {
        val genericPanel = GenericPanelExtraction(panelType = AVL_PANEL)

        assertThat(genericPanel.testedGenes()).isEqualTo(GENERIC_PANEL_ALWAYS_TESTED_GENES)
    }
}