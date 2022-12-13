package com.hartwig.actin.molecular.orange.evidence.known;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.serve.KnownGene;
import com.hartwig.actin.molecular.serve.TestKnownGeneFactory;

import org.junit.Test;

public class GeneLookupTest {

    @Test
    public void canLookupGenes() {
        KnownGene gene1 = TestKnownGeneFactory.builder().gene("gene 1").build();
        KnownGene gene2 = TestKnownGeneFactory.builder().gene("gene 2").build();
        List<KnownGene> knownGenes = Lists.newArrayList(gene1, gene2);

        assertNotNull(GeneLookup.find(knownGenes, "gene 1"));
        assertNotNull(GeneLookup.find(knownGenes, "gene 2"));
        assertNull(GeneLookup.find(knownGenes, "gene 3"));
    }
}