package com.hartwig.actin.molecular.filter

import com.hartwig.actin.molecular.filter.GeneFilterFactory.createAlwaysValid
import com.hartwig.actin.molecular.filter.GeneFilterFactory.createFromKnownGenes
import com.hartwig.serve.datamodel.common.GeneRole
import com.hartwig.serve.datamodel.gene.ImmutableKnownGene
import com.hartwig.serve.datamodel.gene.KnownGene
import org.junit.Assert
import org.junit.Test
import java.util.Set

class GeneFilterFactoryTest {
    @Test
    fun canCreateAlwaysValid() {
        Assert.assertNotNull(createAlwaysValid())
    }

    @Test
    fun canCreateFromKnownGenes() {
        val knownGene: KnownGene = ImmutableKnownGene.builder().gene("gene A").geneRole(GeneRole.UNKNOWN).build()
        val filter = createFromKnownGenes(Set.of(knownGene))
        Assert.assertTrue(filter.include("gene A"))
        Assert.assertFalse(filter.include("gene B"))
    }
}