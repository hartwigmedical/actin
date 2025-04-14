package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.testutil.ResourceLocator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.Test

class PanelSpecificationsFileTest {

    @Test
    fun `Should read from panel gene list TSV and match gene lists on test name regex`() {
        val geneList = PanelSpecificationsFile.create(ResourceLocator.resourceOnClasspath("panel_specifications/panel_specifications.tsv"))
        val oncoPanel = geneList.genesForPanel("oncopanel")
        assertThat(oncoPanel).containsExactly(entry("ABCB1", listOf(MolecularTestTarget.MUTATION)))
        val archer = geneList.genesForPanel("archer")
        assertThat(archer).containsExactly(entry("ALK", MolecularTestTarget.entries))
    }
}