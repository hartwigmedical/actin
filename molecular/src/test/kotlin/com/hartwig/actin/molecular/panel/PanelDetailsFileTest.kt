package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.ExternalLab
import com.hartwig.actin.testutil.ResourceLocator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class PanelDetailsFileTest {

    private val panelDetailsPath = ResourceLocator.resourceOnClasspath("panel_details/panel_details.tsv")

    @Test
    fun `Should read lab configurations correctly from panel details TSV`() {
        val labConfigurations = PanelDetailsFile.readLabConfigurations(panelDetailsPath)
        assertThat(labConfigurations).containsKey("oncopanel")
        assertThat(labConfigurations["oncopanel"]).containsExactlyElementsOf(setOf(ExternalLab.EMC, ExternalLab.NKI))
        assertThat(labConfigurations).containsKey("archer")
        assertThat(labConfigurations["archer"]).containsExactlyElementsOf(setOf(ExternalLab.ANY))
    }

    @Test
    fun `Should throw when panel details TSV contains duplicate test entries`() {
        val path = ResourceLocator.resourceOnClasspath("panel_details/panel_details_with_duplicates.tsv")

        assertThatThrownBy { PanelDetailsFile.readLabConfigurations(path) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("Panel details file contains duplicate test entries. Please ensure each test name is unique.")
    }

    @Test
    fun `Should default to ANY lab when no labs are listed`() {
        val labConfigurations = PanelDetailsFile.readLabConfigurations(panelDetailsPath)
        assertThat(labConfigurations["ngs"]).containsExactlyElementsOf(setOf(ExternalLab.ANY))
    }
}