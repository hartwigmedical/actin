package com.hartwig.actin.molecular.evidence.known

import com.hartwig.serve.datamodel.gene.KnownGene
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GeneLookupTest {

    @Test
    fun `Should lookup genes`() {
        val gene1: KnownGene = TestServeKnownFactory.geneBuilder().gene("gene 1").build()
        val gene2: KnownGene = TestServeKnownFactory.geneBuilder().gene("gene 2").build()
        val knownGenes = setOf(gene1, gene2)

        assertNotNull(GeneLookup.find(knownGenes, "gene 1"))
        assertNotNull(GeneLookup.find(knownGenes, "gene 2"))
        assertNull(GeneLookup.find(knownGenes, "gene 3"))
    }
}