package com.hartwig.actin.molecular.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import com.hartwig.serve.datamodel.common.GeneRole;
import com.hartwig.serve.datamodel.gene.ImmutableKnownGene;
import com.hartwig.serve.datamodel.gene.KnownGene;

import org.junit.Test;

public class GeneFilterFactoryTest {

    @Test
    public void canCreateAlwaysValid() {
        assertNotNull(GeneFilterFactory.createAlwaysValid());
    }

    @Test
    public void canCreateFromKnownGenes() {
        KnownGene knownGene = ImmutableKnownGene.builder().gene("gene A").geneRole(GeneRole.UNKNOWN).build();
        GeneFilter filter = GeneFilterFactory.createFromKnownGenes(Set.of(knownGene));

        assertTrue(filter.include("gene A"));
        assertFalse(filter.include("gene B"));
    }
}