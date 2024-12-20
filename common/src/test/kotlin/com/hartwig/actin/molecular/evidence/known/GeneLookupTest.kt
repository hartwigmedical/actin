package com.hartwig.actin.molecular.evidence.known

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GeneLookupTest {

    @Test
    fun `Should lookup genes`() {
        val gene1 = TestServeKnownFactory.geneBuilder().gene("gene 1").build()
        val gene2 = TestServeKnownFactory.geneBuilder().gene("gene 2").build()
        val knownGenes = setOf(gene1, gene2)

        assertThat(GeneLookup.find(knownGenes, "gene 1")).isNotNull()
        assertThat(GeneLookup.find(knownGenes, "gene 2")).isNotNull()
        assertThat(GeneLookup.find(knownGenes, "gene 3")).isNull()
    }
}