package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import java.time.LocalDate
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

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
        Assertions.assertThat(spec.geneName).isEqualTo(gene)
        Assertions.assertThat(spec.targets).isEqualTo(listOf(MolecularTestTarget.MUTATION))
    }

    @Test
    fun `Should throw IllegalStateException gene has no targets`() {
        Assertions.assertThatThrownBy {
            geneEntry.copy(mutation = false).toPanelGeneSpecification()
        }.isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("Targets for test $test version $versionDate and gene $gene are empty")
    }
}