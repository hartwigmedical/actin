package com.hartwig.actin.molecular.orange.evidence.known

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory
import com.hartwig.hmftools.datamodel.linx.LinxFusion
import com.hartwig.serve.datamodel.fusion.KnownFusion
import org.junit.Assert
import org.junit.Test

class FusionLookupTest {
    @Test
    fun canLookupFusions() {
        val fusion1: KnownFusion = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").build()
        val fusion2: KnownFusion = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").minExonUp(3).maxExonUp(3).build()
        val fusion3: KnownFusion = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").minExonDown(4).maxExonDown(4).build()
        val fusion4: KnownFusion = TestServeKnownFactory.fusionBuilder()
            .geneUp("up")
            .geneDown("down")
            .minExonUp(3)
            .maxExonUp(3)
            .minExonDown(4)
            .maxExonDown(4)
            .build()
        val knownFusions: MutableList<KnownFusion> = Lists.newArrayList(fusion1, fusion2, fusion3, fusion4)
        val broadMatch: LinxFusion = TestLinxFactory.fusionBuilder().geneStart("up").geneEnd("down").fusedExonUp(2).fusedExonDown(5).build()
        Assert.assertEquals(fusion1, FusionLookup.find(knownFusions, broadMatch))
        val specificUpMatch: LinxFusion = TestLinxFactory.fusionBuilder().geneStart("up").geneEnd("down").fusedExonUp(3).fusedExonDown(5).build()
        Assert.assertEquals(fusion2, FusionLookup.find(knownFusions, specificUpMatch))
        val specificDownMatch: LinxFusion = TestLinxFactory.fusionBuilder().geneStart("up").geneEnd("down").fusedExonUp(2).fusedExonDown(4).build()
        Assert.assertEquals(fusion3, FusionLookup.find(knownFusions, specificDownMatch))
        val specificMatch: LinxFusion = TestLinxFactory.fusionBuilder().geneStart("up").geneEnd("down").fusedExonUp(3).fusedExonDown(4).build()
        Assert.assertEquals(fusion4, FusionLookup.find(knownFusions, specificMatch))
        val noMatch: LinxFusion = TestLinxFactory.fusionBuilder().geneStart("down").geneEnd("up").build()
        Assert.assertNull(FusionLookup.find(knownFusions, noMatch))
    }
}