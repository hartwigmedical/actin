package com.hartwig.actin.molecular.orange.evidence.matching

import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory
import com.hartwig.actin.molecular.orange.evidence.known.TestServeKnownFactory
import com.hartwig.hmftools.datamodel.linx.LinxFusion
import com.hartwig.serve.datamodel.fusion.KnownFusion
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FusionMatchingTest {

    @Test
    fun canMatchFusions() {
        val generic: KnownFusion = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").build()
        val exonAware: KnownFusion =
            TestServeKnownFactory.fusionBuilder().from(generic).minExonUp(3).maxExonUp(4).minExonDown(6).maxExonDown(7).build()

        val noMatch: LinxFusion = TestLinxFactory.fusionBuilder().geneStart("down").geneEnd("up").build()
        assertFalse(FusionMatching.isGeneMatch(generic, noMatch))
        assertFalse(FusionMatching.isGeneMatch(exonAware, noMatch))

        val genericMatch: LinxFusion =
            TestLinxFactory.fusionBuilder().geneStart("up").geneEnd("down").fusedExonUp(0).fusedExonDown(0).build()
        assertTrue(FusionMatching.isGeneMatch(generic, genericMatch))
        assertTrue(FusionMatching.isExonMatch(generic, genericMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonUp(generic, genericMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonDown(generic, genericMatch))
        assertTrue(FusionMatching.isGeneMatch(exonAware, genericMatch))
        assertFalse(FusionMatching.isExonMatch(exonAware, genericMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonUp(exonAware, genericMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonDown(exonAware, genericMatch))

        val exactMatch: LinxFusion = TestLinxFactory.fusionBuilder().from(genericMatch).fusedExonUp(4).fusedExonDown(6).build()
        assertTrue(FusionMatching.isGeneMatch(generic, exactMatch))
        assertTrue(FusionMatching.isExonMatch(generic, exactMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonUp(generic, exactMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonDown(generic, exactMatch))
        assertTrue(FusionMatching.isGeneMatch(exonAware, exactMatch))
        assertTrue(FusionMatching.isExonMatch(exonAware, exactMatch))
        assertTrue(FusionMatching.explicitlyMatchesExonUp(exonAware, exactMatch))
        assertTrue(FusionMatching.explicitlyMatchesExonDown(exonAware, exactMatch))

        val exonUpMatch: LinxFusion = TestLinxFactory.fusionBuilder().from(genericMatch).fusedExonUp(3).fusedExonDown(8).build()
        assertTrue(FusionMatching.isGeneMatch(generic, exonUpMatch))
        assertTrue(FusionMatching.isExonMatch(generic, exonUpMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonUp(generic, exonUpMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonDown(generic, exonUpMatch))
        assertTrue(FusionMatching.isGeneMatch(exonAware, exonUpMatch))
        assertFalse(FusionMatching.isExonMatch(exonAware, exonUpMatch))
        assertTrue(FusionMatching.explicitlyMatchesExonUp(exonAware, exonUpMatch))
        assertFalse(FusionMatching.explicitlyMatchesExonDown(exonAware, exonUpMatch))

        val exonDownMatch: LinxFusion = TestLinxFactory.fusionBuilder().from(genericMatch).fusedExonUp(2).fusedExonDown(7).build()
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