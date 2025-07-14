package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.ExternalLab
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.PanelTestSpecification
import com.hartwig.actin.testutil.ResourceLocator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import java.time.LocalDate

private val ANY_LAB = setOf(ExternalLab.ANY)
private val LAB_CONFIG = mapOf("test" to ANY_LAB)

class PanelSpecificationsFileTest {

    @Test
    fun `Should read from panel gene list TSV and match gene lists on test name regex`() {
        val geneList =
            PanelSpecificationsFile.create(ResourceLocator.resourceOnClasspath("panel_specifications/panel_specifications.tsv"), LAB_CONFIG)
        val oncoPanel = geneList.panelSpecification(PanelTestSpecification("oncopanel", LocalDate.of(2022, 1, 1), ANY_LAB))
        assertThat(oncoPanel.testsGene("ABCB1") { it == listOf(MolecularTestTarget.MUTATION) }).isTrue()
        assertThat(oncoPanel.testsGene("EGFR") { it == listOf(MolecularTestTarget.MUTATION) }).isFalse()
        assertThat(oncoPanel.testsGene("ALK") { it == listOf(MolecularTestTarget.MUTATION) }).isFalse()
        assertThat(oncoPanel.testsGene("ABCB1") { it == listOf(MolecularTestTarget.FUSION) }).isFalse()
        val archer = geneList.panelSpecification(PanelTestSpecification("archer", null, ANY_LAB))
        assertThat(archer.testsGene("ALK") { it == MolecularTestTarget.entries }).isTrue()
        assertThat(archer.testsGene("ROS1") { it == listOf(MolecularTestTarget.MUTATION, MolecularTestTarget.FUSION) }).isTrue()
        assertThat(archer.testsGene("ABCB1") { it == MolecularTestTarget.entries }).isFalse()
    }

    @Test
    fun `Should group genes correctly by test specification`() {
        val geneList =
            PanelSpecificationsFile.create(ResourceLocator.resourceOnClasspath("panel_specifications/panel_specifications.tsv"), LAB_CONFIG)
        val (oldOncoPanel, newOncoPanel) = listOf(LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1)).map {
            geneList.panelSpecification(PanelTestSpecification("oncopanel", it, ANY_LAB))
        }
        assertThat(oldOncoPanel.testsGene("ABCB1") { it == listOf(MolecularTestTarget.MUTATION) }).isTrue()
        assertThat(oldOncoPanel.testsGene("EGFR") { it == listOf(MolecularTestTarget.MUTATION) }).isFalse()
        assertThat(newOncoPanel.testsGene("ABCB1") { it == listOf(MolecularTestTarget.MUTATION) }).isFalse()
        assertThat(newOncoPanel.testsGene("EGFR") { it == listOf(MolecularTestTarget.MUTATION) }).isTrue()
    }

    @Test
    fun `Should throw illegal state exception when test name not found in panel details file`() {
        assertThatThrownBy {
            PanelSpecificationsFile.create(ResourceLocator.resourceOnClasspath("panel_specifications/panel_specifications.tsv"), emptyMap())
        }.isInstanceOfAny(IllegalStateException::class.java)
            .hasMessageContaining("No lab configuration found for test 'oncopanel'. Please add it to panel_details.tsv")
    }
}