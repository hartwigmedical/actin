package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.testutil.ResourceLocator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PanelSpecificationsFileTest {

    @Test
    fun `Should read from panel gene list TSV and match gene lists on test name regex`() {
        val geneList = PanelSpecificationsFile.create(ResourceLocator.resourceOnClasspath("panel_specifications/panel_specifications.tsv"))
        val oncoPanel = geneList.genesForPanel("oncopanel")
        assertThat(oncoPanel.testsGene("ABCB1") { it == listOf(MolecularTestTarget.MUTATION) }).isTrue()
        assertThat(oncoPanel.testsGene("ALK") { it == listOf(MolecularTestTarget.MUTATION) }).isFalse()
        assertThat(oncoPanel.testsGene("ABCB1") { it == listOf(MolecularTestTarget.FUSION) }).isFalse()
        val archer = geneList.genesForPanel("archer")
        assertThat(archer.testsGene("ALK") { it == MolecularTestTarget.entries }).isTrue()
        assertThat(archer.testsGene("ABCB1") { it == MolecularTestTarget.entries }).isFalse()
    }
}