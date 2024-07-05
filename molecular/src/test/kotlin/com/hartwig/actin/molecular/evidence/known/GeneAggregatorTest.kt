package com.hartwig.actin.molecular.evidence.known

import com.hartwig.serve.datamodel.common.GeneRole
import com.hartwig.serve.datamodel.gene.ImmutableKnownGene
import com.hartwig.serve.datamodel.gene.KnownGene
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GeneAggregatorTest {

    @Test
    fun `Should use both gene roles when onco, both and unknown in set`() {
        assertThat(GeneAggregator.aggregate(setOf(ONCO_GENE, BOTH_GENE, UNKNOWN_GENE))).containsOnly(BOTH_GENE)
    }

    @Test
    fun `Should use onco gene role when onco and unknown in set`() {
        assertThat(GeneAggregator.aggregate(setOf(ONCO_GENE, UNKNOWN_GENE))).containsOnly(ONCO_GENE)
    }

    @Test
    fun `Should use unknown gene role when only role present`() {
        assertThat(GeneAggregator.aggregate(setOf(UNKNOWN_GENE))).containsOnly(UNKNOWN_GENE)
    }

    @Test
    fun `Should only aggregate when required`() {
        val anotherGene = ImmutableKnownGene.copyOf(ONCO_GENE).withGene("another_gene")
        assertThat(GeneAggregator.aggregate(setOf(ONCO_GENE, anotherGene))).containsOnly(ONCO_GENE, anotherGene)
    }

    companion object {
        private const val GENE: String = "gene"
        private val ONCO_GENE = gene(GeneRole.ONCO)
        private val BOTH_GENE = gene(GeneRole.BOTH)
        private val UNKNOWN_GENE = gene(GeneRole.UNKNOWN)

        private fun gene(role: GeneRole): KnownGene {
            return ImmutableKnownGene.builder().gene(GENE).geneRole(role).build()
        }
    }
}