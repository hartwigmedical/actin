package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.ExternalLab
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.testutil.ResourceLocator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import java.time.LocalDate

class PanelGeneEntryTest {

    private val test = "oncopanel"
    private val versionDate = LocalDate.of(2022, 1, 1)
    private val gene = "EGFR"
    private val labConfigurations = mapOf(test to setOf(ExternalLab.ANY))

    private val geneEntry = PanelGeneEntry(
        testName = test,
        versionDate = versionDate,
        gene = gene,
        mutation = true,
        amplification = false,
        deletion = false,
        fusion = false
    )

    @Test
    fun `Should return correct gene specification`() {
        val spec = geneEntry.toPanelGeneSpecification()
        assertThat(spec.geneName).isEqualTo(gene)
        assertThat(spec.targets).isEqualTo(listOf(MolecularTestTarget.MUTATION))
    }

    @Test
    fun `Should throw IllegalStateException gene has no targets`() {
        assertThatThrownBy {
            geneEntry.copy(mutation = false).toPanelGeneSpecification()
        }.isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("Targets for test $test version $versionDate and gene $gene are empty")
    }

    @Test
    fun `Should return correct test specification`() {
        val spec = geneEntry.toPanelTestSpecification(labConfigurations)
        assertThat(spec.testName).isEqualTo(test)
        assertThat(spec.version).isEqualTo(versionDate)
        assertThat(spec.configurableForLabs).isEqualTo(setOf(ExternalLab.ANY))
    }

    @Test
    fun `Should throw IllegalStateException if test name not found in lab configurations`() {
        assertThatThrownBy {
            geneEntry.toPanelTestSpecification(emptyMap())
        }.isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("No lab configuration found for test '$test'. Please add it to panel_details.tsv")
    }


    @Test
    fun `Should throw illegal state exception when test name not found in panel details file`() {
        assertThatThrownBy {
            PanelSpecificationsFile.create(ResourceLocator.resourceOnClasspath("panel_specifications/panel_specifications.tsv"), emptyMap())
        }.isInstanceOfAny(IllegalStateException::class.java)
            .hasMessageContaining("No lab configuration found for test 'oncopanel'. Please add it to panel_details.tsv")
    }
}