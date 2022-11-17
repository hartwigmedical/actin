package com.hartwig.actin.molecular.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;

import org.junit.Test;

public class GeneFilterFactoryTest {

    @Test
    public void canCreateAlwaysValid() {
        assertNotNull(GeneFilterFactory.createAlwaysValid());
    }

    @Test
    public void canCreateFromKnownGenes() {
        KnownGene knownGene = TestKnownGeneFactory.builder().gene("gene A").build();
        GeneFilter filter = GeneFilterFactory.createFromKnownGenes(Lists.newArrayList(knownGene));

        assertTrue(filter.include("gene A"));
        assertFalse(filter.include("gene B"));
    }
}