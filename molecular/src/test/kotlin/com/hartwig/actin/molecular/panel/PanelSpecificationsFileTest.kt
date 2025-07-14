package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.PanelTestSpecification
import com.hartwig.actin.testutil.ResourceLocator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class PanelSpecificationsFileTest {

    @Test
    fun `Should read from panel gene list TSV and match gene lists on test name regex`() {
        val geneList =
            PanelSpecificationsFile.create(ResourceLocator.resourceOnClasspath("panel_specifications/panel_specifications.tsv"))
        val oncoPanel = geneList.panelSpecification(PanelTestSpecification("oncopanel", LocalDate.of(2022, 1, 1)))
        assertThat(oncoPanel.testsGene("ABCB1") { it == listOf(MolecularTestTarget.MUTATION) }).isTrue()
        assertThat(oncoPanel.testsGene("EGFR") { it == listOf(MolecularTestTarget.MUTATION) }).isFalse()
        assertThat(oncoPanel.testsGene("ALK") { it == listOf(MolecularTestTarget.MUTATION) }).isFalse()
        assertThat(oncoPanel.testsGene("ABCB1") { it == listOf(MolecularTestTarget.FUSION) }).isFalse()
        val archer = geneList.panelSpecification(PanelTestSpecification("archer"))
        assertThat(archer.testsGene("ALK") { it == MolecularTestTarget.entries }).isTrue()
        assertThat(archer.testsGene("ROS1") { it == listOf(MolecularTestTarget.MUTATION, MolecularTestTarget.FUSION) }).isTrue()
        assertThat(archer.testsGene("ABCB1") { it == MolecularTestTarget.entries }).isFalse()
    }

    @Test
    fun `Should group genes correctly by test specification`() {
        val geneList = PanelSpecificationsFile.create(ResourceLocator.resourceOnClasspath("panel_specifications/panel_specifications.tsv"))
        val (oldOncoPanel, newOncoPanel) = listOf(LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1)).map {
            geneList.panelSpecification(PanelTestSpecification("oncopanel", it))
        }
        assertThat(oldOncoPanel.testsGene("ABCB1") { it == listOf(MolecularTestTarget.MUTATION) }).isTrue()
        assertThat(oldOncoPanel.testsGene("EGFR") { it == listOf(MolecularTestTarget.MUTATION) }).isFalse()
        assertThat(newOncoPanel.testsGene("ABCB1") { it == listOf(MolecularTestTarget.MUTATION) }).isFalse()
        assertThat(newOncoPanel.testsGene("EGFR") { it == listOf(MolecularTestTarget.MUTATION) }).isTrue()
    }
}