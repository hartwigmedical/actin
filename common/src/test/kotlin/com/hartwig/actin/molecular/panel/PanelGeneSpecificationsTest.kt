package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencingTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.panel.TestVersion
import com.hartwig.actin.molecular.filter.AlwaysValidFilter
import com.hartwig.actin.testutil.ResourceLocator
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate

class PanelGeneSpecificationsTest {

    val panelSpecs: String = Files.readString(Path.of(ResourceLocator.resourceOnClasspath("panel_specifications/panel_specifications.tsv")))
    val geneList = PanelGeneSpecifications.create(panelSpecs, AlwaysValidFilter())

    @Test
    fun `Should read from panel gene list TSV and match gene lists on test name regex`() {
        val oncoPanel = geneList.panelTargetSpecification(SequencingTest("oncopanel"), TestVersion(LocalDate.of(2022, 1, 1)))
        Assertions.assertThat(oncoPanel.testsGene("ABCB1") { it == listOf(MolecularTestTarget.MUTATION) }).isTrue()
        Assertions.assertThat(oncoPanel.testsGene("EGFR") { it == listOf(MolecularTestTarget.MUTATION) }).isFalse()
        Assertions.assertThat(oncoPanel.testsGene("ALK") { it == listOf(MolecularTestTarget.MUTATION) }).isFalse()
        Assertions.assertThat(oncoPanel.testsGene("ABCB1") { it == listOf(MolecularTestTarget.FUSION) }).isFalse()
        val archer = geneList.panelTargetSpecification(SequencingTest("archer"), TestVersion(null))
        Assertions.assertThat(archer.testsGene("ALK") { it == MolecularTestTarget.entries }).isTrue()
        Assertions.assertThat(archer.testsGene("ROS1") { it == listOf(MolecularTestTarget.MUTATION, MolecularTestTarget.FUSION) }).isTrue()
        Assertions.assertThat(archer.testsGene("ABCB1") { it == MolecularTestTarget.entries }).isFalse()
    }

    @Test
    fun `Should group genes correctly by test specification`() {
        val (oldOncoPanel, newOncoPanel) = listOf(LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1)).map {
            geneList.panelTargetSpecification(SequencingTest("oncopanel"), TestVersion(it))
        }
        Assertions.assertThat(oldOncoPanel.testsGene("ABCB1") { it == listOf(MolecularTestTarget.MUTATION) }).isTrue()
        Assertions.assertThat(oldOncoPanel.testsGene("EGFR") { it == listOf(MolecularTestTarget.MUTATION) }).isFalse()
        Assertions.assertThat(newOncoPanel.testsGene("ABCB1") { it == listOf(MolecularTestTarget.MUTATION) }).isFalse()
        Assertions.assertThat(newOncoPanel.testsGene("EGFR") { it == listOf(MolecularTestTarget.MUTATION) }).isTrue()
    }

}
