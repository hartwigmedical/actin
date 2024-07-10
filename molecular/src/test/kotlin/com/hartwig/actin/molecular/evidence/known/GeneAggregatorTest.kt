package com.hartwig.actin.molecular.evidence.known

import com.hartwig.serve.datamodel.common.GeneRole
import com.hartwig.serve.datamodel.gene.ImmutableKnownGene
import com.hartwig.serve.datamodel.gene.KnownGene
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val GENE: String = "gene"

class GeneAggregatorTest {

    private val oncoGene = gene(GeneRole.ONCO)
    private val bothGene = gene(GeneRole.BOTH)
    private val unknownGene = gene(GeneRole.UNKNOWN)

    @Test
    fun `Should use both gene roles when onco, both and unknown in set`() {
        assertThat(GeneAggregator.aggregate(setOf(oncoGene, bothGene, unknownGene))).containsOnly(bothGene)
    }

    @Test
    fun `Should use onco gene role when onco and unknown in set`() {
        assertThat(GeneAggregator.aggregate(setOf(oncoGene, unknownGene))).containsOnly(oncoGene)
    }

    @Test
    fun `Should use unknown gene role when only role present`() {
        assertThat(GeneAggregator.aggregate(setOf(unknownGene))).containsOnly(unknownGene)
    }

    @Test
    fun `Should only aggregate when required`() {
        val anotherGene = ImmutableKnownGene.copyOf(oncoGene).withGene("another_gene")
        assertThat(GeneAggregator.aggregate(setOf(oncoGene, anotherGene))).containsOnly(oncoGene, anotherGene)
    }

    private fun gene(role: GeneRole): KnownGene {
        return ImmutableKnownGene.builder().gene(GENE).geneRole(role).build()
    }
}