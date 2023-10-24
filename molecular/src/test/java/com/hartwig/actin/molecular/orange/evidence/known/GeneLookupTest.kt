package com.hartwig.actin.molecular.orange.evidence.known

import com.hartwig.serve.datamodel.gene.KnownGene
import org.junit.Assert
import org.junit.Test
import java.util.Set

class GeneLookupTest {
    @Test
    fun canLookupGenes() {
        val gene1: KnownGene? = TestServeKnownFactory.geneBuilder().gene("gene 1").build()
        val gene2: KnownGene? = TestServeKnownFactory.geneBuilder().gene("gene 2").build()
        val knownGenes = Set.of(gene1, gene2)
        Assert.assertNotNull(GeneLookup.find(knownGenes, "gene 1"))
        Assert.assertNotNull(GeneLookup.find(knownGenes, "gene 2"))
        Assert.assertNull(GeneLookup.find(knownGenes, "gene 3"))
    }
}