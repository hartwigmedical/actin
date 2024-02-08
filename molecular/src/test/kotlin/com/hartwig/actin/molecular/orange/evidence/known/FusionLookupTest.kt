package com.hartwig.actin.molecular.orange.evidence.known

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.orange.evidence.TestMolecularFactory.minimalFusion
import com.hartwig.serve.datamodel.fusion.KnownFusion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FusionLookupTest {

    @Test
    fun `Should lookup fusions`() {
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

        val broadMatch = minimalFusion().copy(geneStart = "up", geneEnd = "down", fusedExonUp = 2, fusedExonDown = 5)
        assertEquals(fusion1, FusionLookup.find(knownFusions, broadMatch))

        val specificUpMatch = minimalFusion().copy(geneStart = "up", geneEnd = "down", fusedExonUp = 3, fusedExonDown = 5)
        assertEquals(fusion2, FusionLookup.find(knownFusions, specificUpMatch))

        val specificDownMatch = minimalFusion().copy(geneStart = "up", geneEnd = "down", fusedExonUp = 2, fusedExonDown = 4)
        assertEquals(fusion3, FusionLookup.find(knownFusions, specificDownMatch))

        val specificMatch = minimalFusion().copy(geneStart = "up", geneEnd = "down", fusedExonUp = 3, fusedExonDown = 4)
        assertEquals(fusion4, FusionLookup.find(knownFusions, specificMatch))

        val noMatch = minimalFusion().copy(geneStart = "down", geneEnd = "up")
        assertNull(FusionLookup.find(knownFusions, noMatch))
    }
}