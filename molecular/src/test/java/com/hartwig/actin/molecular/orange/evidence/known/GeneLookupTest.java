package com.hartwig.actin.molecular.orange.evidence.known;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Set;

import com.hartwig.serve.datamodel.gene.KnownGene;

import org.junit.Test;

public class GeneLookupTest {

    @Test
    public void canLookupGenes() {
        KnownGene gene1 = TestServeKnownFactory.geneBuilder().gene("gene 1").build();
        KnownGene gene2 = TestServeKnownFactory.geneBuilder().gene("gene 2").build();
        Set<KnownGene> knownGenes = Set.of(gene1, gene2);

        assertNotNull(GeneLookup.find(knownGenes, "gene 1"));
        assertNotNull(GeneLookup.find(knownGenes, "gene 2"));
        assertNull(GeneLookup.find(knownGenes, "gene 3"));
    }
}