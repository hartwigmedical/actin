package com.hartwig.actin.molecular.datamodel.panel.generic

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class GenericPanelTest {

    @Test
    fun `Should identify tested genes`() {
        val genericPanel = GenericPanel(
            GenericPanelType.AVL,
            date = LocalDate.of(2021, 1, 1),
        )

        assertThat(genericPanel.testedGenes()).containsExactlyInAnyOrder("EGFR", "BRAF")
    }
}