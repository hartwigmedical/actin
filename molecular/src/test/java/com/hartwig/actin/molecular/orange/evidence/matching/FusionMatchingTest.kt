package com.hartwig.actin.molecular.orange.evidence.matching

import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory
import com.hartwig.actin.molecular.orange.evidence.known.TestServeKnownFactory
import com.hartwig.hmftools.datamodel.linx.LinxFusion
import com.hartwig.serve.datamodel.fusion.KnownFusion
import org.junit.Assert
import org.junit.Test

class FusionMatchingTest {
    @Test
    fun canMatchFusions() {
        val generic: KnownFusion? = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").build()
        val exonAware: KnownFusion? = TestServeKnownFactory.fusionBuilder().from(generic).minExonUp(3).maxExonUp(4).minExonDown(6).maxExonDown(7).build()
        val noMatch: LinxFusion? = TestLinxFactory.fusionBuilder().geneStart("down").geneEnd("up").build()
        Assert.assertFalse(FusionMatching.isGeneMatch(generic, noMatch))
        Assert.assertFalse(FusionMatching.isGeneMatch(exonAware, noMatch))
        val genericMatch: LinxFusion? = TestLinxFactory.fusionBuilder().geneStart("up").geneEnd("down").fusedExonUp(0).fusedExonDown(0).build()
        Assert.assertTrue(FusionMatching.isGeneMatch(generic, genericMatch))
        Assert.assertTrue(FusionMatching.isExonMatch(generic, genericMatch))
        Assert.assertFalse(FusionMatching.explicitlyMatchesExonUp(generic, genericMatch))
        Assert.assertFalse(FusionMatching.explicitlyMatchesExonDown(generic, genericMatch))
        Assert.assertTrue(FusionMatching.isGeneMatch(exonAware, genericMatch))
        Assert.assertFalse(FusionMatching.isExonMatch(exonAware, genericMatch))
        Assert.assertFalse(FusionMatching.explicitlyMatchesExonUp(exonAware, genericMatch))
        Assert.assertFalse(FusionMatching.explicitlyMatchesExonDown(exonAware, genericMatch))
        val exactMatch: LinxFusion? = TestLinxFactory.fusionBuilder().from(genericMatch).fusedExonUp(4).fusedExonDown(6).build()
        Assert.assertTrue(FusionMatching.isGeneMatch(generic, exactMatch))
        Assert.assertTrue(FusionMatching.isExonMatch(generic, exactMatch))
        Assert.assertFalse(FusionMatching.explicitlyMatchesExonUp(generic, exactMatch))
        Assert.assertFalse(FusionMatching.explicitlyMatchesExonDown(generic, exactMatch))
        Assert.assertTrue(FusionMatching.isGeneMatch(exonAware, exactMatch))
        Assert.assertTrue(FusionMatching.isExonMatch(exonAware, exactMatch))
        Assert.assertTrue(FusionMatching.explicitlyMatchesExonUp(exonAware, exactMatch))
        Assert.assertTrue(FusionMatching.explicitlyMatchesExonDown(exonAware, exactMatch))
        val exonUpMatch: LinxFusion? = TestLinxFactory.fusionBuilder().from(genericMatch).fusedExonUp(3).fusedExonDown(8).build()
        Assert.assertTrue(FusionMatching.isGeneMatch(generic, exonUpMatch))
        Assert.assertTrue(FusionMatching.isExonMatch(generic, exonUpMatch))
        Assert.assertFalse(FusionMatching.explicitlyMatchesExonUp(generic, exonUpMatch))
        Assert.assertFalse(FusionMatching.explicitlyMatchesExonDown(generic, exonUpMatch))
        Assert.assertTrue(FusionMatching.isGeneMatch(exonAware, exonUpMatch))
        Assert.assertFalse(FusionMatching.isExonMatch(exonAware, exonUpMatch))
        Assert.assertTrue(FusionMatching.explicitlyMatchesExonUp(exonAware, exonUpMatch))
        Assert.assertFalse(FusionMatching.explicitlyMatchesExonDown(exonAware, exonUpMatch))
        val exonDownMatch: LinxFusion? = TestLinxFactory.fusionBuilder().from(genericMatch).fusedExonUp(2).fusedExonDown(7).build()
        Assert.assertTrue(FusionMatching.isGeneMatch(generic, exonDownMatch))
        Assert.assertTrue(FusionMatching.isExonMatch(generic, exonDownMatch))
        Assert.assertFalse(FusionMatching.explicitlyMatchesExonUp(generic, exonDownMatch))
        Assert.assertFalse(FusionMatching.explicitlyMatchesExonDown(generic, exonDownMatch))
        Assert.assertTrue(FusionMatching.isGeneMatch(exonAware, exonDownMatch))
        Assert.assertFalse(FusionMatching.isExonMatch(exonAware, exonDownMatch))
        Assert.assertFalse(FusionMatching.explicitlyMatchesExonUp(exonAware, exonDownMatch))
        Assert.assertTrue(FusionMatching.explicitlyMatchesExonDown(exonAware, exonDownMatch))
    }
}