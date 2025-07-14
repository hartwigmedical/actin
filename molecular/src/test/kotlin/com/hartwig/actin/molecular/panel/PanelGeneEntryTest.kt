package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import java.time.LocalDate

class PanelGeneEntryTest {

    private val test = "oncopanel"
    private val versionDate = LocalDate.of(2022, 1, 1)
    private val gene = "EGFR"

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
}