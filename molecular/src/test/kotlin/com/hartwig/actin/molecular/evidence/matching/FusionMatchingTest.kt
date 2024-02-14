package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.molecular.evidence.TestMolecularFactory.minimalFusion
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import com.hartwig.serve.datamodel.fusion.KnownFusion
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FusionMatchingTest {

    @Test
    fun `Should match fusions`() {
        val generic: KnownFusion = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").build()
        val exonAware: KnownFusion =
            TestServeKnownFactory.fusionBuilder().from(generic).minExonUp(3).maxExonUp(4).minExonDown(6).maxExonDown(7).build()

        val noMatch = minimalFusion().copy(geneStart = "down", geneEnd = "up")
        assertFalse(FusionMatching.isGeneMatch(generic, noMatch))
        assertFalse(FusionMatching.isGeneMatch(exonAware, noMatch))

        val genericMatch = minimalFusion().copy(geneStart = "up", geneEnd = "down")
        assertTrue(FusionMatching.isGeneMatch(generic, genericMatch))
        assertTrue(FusionMatching.isExonMatch(generic, genericMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonUp(generic, genericMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonDown(generic, genericMatch))
        assertTrue(FusionMatching.isGeneMatch(exonAware, genericMatch))
        assertFalse(FusionMatching.isExonMatch(exonAware, genericMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonUp(exonAware, genericMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonDown(exonAware, genericMatch))

        val exactMatch = genericMatch.copy(fusedExonUp = 4, fusedExonDown = 6)
        assertTrue(FusionMatching.isGeneMatch(generic, exactMatch))
        assertTrue(FusionMatching.isExonMatch(generic, exactMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonUp(generic, exactMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonDown(generic, exactMatch))
        assertTrue(FusionMatching.isGeneMatch(exonAware, exactMatch))
        assertTrue(FusionMatching.isExonMatch(exonAware, exactMatch))
        assertTrue(FusionMatching.explicitlyMatchesExonUp(exonAware, exactMatch))
        assertTrue(FusionMatching.explicitlyMatchesExonDown(exonAware, exactMatch))

        val exonUpMatch = genericMatch.copy(fusedExonUp = 3, fusedExonDown = 8)
        assertTrue(FusionMatching.isGeneMatch(generic, exonUpMatch))
        assertTrue(FusionMatching.isExonMatch(generic, exonUpMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonUp(generic, exonUpMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonDown(generic, exonUpMatch))
        assertTrue(FusionMatching.isGeneMatch(exonAware, exonUpMatch))
        assertFalse(FusionMatching.isExonMatch(exonAware, exonUpMatch))
        assertTrue(FusionMatching.explicitlyMatchesExonUp(exonAware, exonUpMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonDown(exonAware, exonUpMatch))

        val exonDownMatch = genericMatch.copy(fusedExonUp = 2, fusedExonDown = 7)
        assertTrue(FusionMatching.isGeneMatch(generic, exonDownMatch))
        assertTrue(FusionMatching.isExonMatch(generic, exonDownMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonUp(generic, exonDownMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonDown(generic, exonDownMatch))
        assertTrue(FusionMatching.isGeneMatch(exonAware, exonDownMatch))
        assertFalse(FusionMatching.isExonMatch(exonAware, exonDownMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonUp(exonAware, exonDownMatch))
        assertTrue(FusionMatching.explicitlyMatchesExonDown(exonAware, exonDownMatch))
    }
}