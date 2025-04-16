package com.hartwig.actin.datamodel.molecular

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class PanelSpecificationsTest {

    @Test
    fun `Should evaluate target predicate when gene is in panel specification`() {
        val targets = listOf(MolecularTestTarget.MUTATION)
        val gene = "gene"
        val specification = PanelSpecification(mapOf(gene to targets))
        assertThat(specification.testsGene(gene) { it == targets }).isTrue()
        assertThat(specification.testsGene(gene) { it == listOf(MolecularTestTarget.FUSION) }).isFalse()
    }

    @Test
    fun `Should return false if gene is not in specification`() {
        val specification = PanelSpecification(mapOf("gene" to emptyList()))
        assertThat(specification.testsGene("another gene") { true })
    }

    @Test
    fun `Should resolve a panels specification from the set of all specification by name`() {
        val panelName = "panel"
        val geneName = "gene"
        val specifications =
            PanelSpecifications(mapOf(panelName to listOf(PanelGeneSpecification(geneName, listOf(MolecularTestTarget.MUTATION)))))
        val specification = specifications.genesForPanel(panelName)
        assertThat(specification.testsGene(geneName) { it == listOf(MolecularTestTarget.MUTATION) })
    }

    @Test
    fun `Should throw illegal state exception when a panel name is not found`() {
        assertThatThrownBy {
            val specifications = PanelSpecifications(emptyMap())
            specifications.genesForPanel("panel")
        }.isInstanceOfAny(IllegalStateException::class.java)
    }
}