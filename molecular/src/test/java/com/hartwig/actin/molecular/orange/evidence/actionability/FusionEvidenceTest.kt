package com.hartwig.actin.molecular.orange.evidence.actionability

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory
import com.hartwig.hmftools.datamodel.linx.LinxFusion
import com.hartwig.hmftools.datamodel.linx.LinxFusionType
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.fusion.ActionableFusion
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent
import org.junit.Assert
import org.junit.Test

class FusionEvidenceTest {
    @Test
    fun canDeterminePromiscuousFusionEvidence() {
        val gene1: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.FUSION).gene("gene 1").build()
        val gene2: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.ANY_MUTATION).gene("gene 2").build()
        val gene3: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.INACTIVATION).gene("gene 1").build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().genes(Lists.newArrayList(gene1, gene2, gene3)).build()
        val fusionEvidence: FusionEvidence = FusionEvidence.Companion.create(actionable)
        val reportedFusionGene1: LinxFusion = TestLinxFactory.fusionBuilder().geneStart("gene 1").reportedType(LinxFusionType.PROMISCUOUS_5).reported(true).build()
        val evidenceMatchGene1 = fusionEvidence.findMatches(reportedFusionGene1)
        Assert.assertEquals(1, evidenceMatchGene1.size.toLong())
        Assert.assertTrue(evidenceMatchGene1.contains(gene1))
        val unreportedFusionGene1: LinxFusion = TestLinxFactory.fusionBuilder().geneStart("gene 1").reportedType(LinxFusionType.PROMISCUOUS_5).reported(false).build()
        Assert.assertTrue(fusionEvidence.findMatches(unreportedFusionGene1).isEmpty())
        val wrongTypeFusionGene1: LinxFusion = TestLinxFactory.fusionBuilder().geneStart("gene 1").reportedType(LinxFusionType.PROMISCUOUS_3).reported(true).build()
        Assert.assertTrue(fusionEvidence.findMatches(wrongTypeFusionGene1).isEmpty())
        val reportedFusionGene2: LinxFusion = TestLinxFactory.fusionBuilder().geneEnd("gene 2").reportedType(LinxFusionType.PROMISCUOUS_3).reported(true).build()
        val evidenceMatchGene2 = fusionEvidence.findMatches(reportedFusionGene2)
        Assert.assertEquals(1, evidenceMatchGene2.size.toLong())
        Assert.assertTrue(evidenceMatchGene2.contains(gene2))
    }

    @Test
    fun canDetermineEvidenceForKnownFusions() {
        val actionableFusion: ActionableFusion = TestServeActionabilityFactory.fusionBuilder().geneUp("up").geneDown("down").minExonUp(4).maxExonUp(6).build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().addFusions(actionableFusion).build()
        val fusionEvidence: FusionEvidence = FusionEvidence.Companion.create(actionable)
        val match: LinxFusion = TestLinxFactory.fusionBuilder().geneStart("up").geneEnd("down").fusedExonUp(5).reported(true).build()
        val evidences = fusionEvidence.findMatches(match)
        Assert.assertEquals(1, evidences.size.toLong())
        Assert.assertTrue(evidences.contains(actionableFusion))
        val notReported: LinxFusion = TestLinxFactory.fusionBuilder().geneStart("up").geneEnd("down").fusedExonUp(5).reported(false).build()
        Assert.assertTrue(fusionEvidence.findMatches(notReported).isEmpty())
        val wrongExon: LinxFusion = TestLinxFactory.fusionBuilder().geneStart("up").geneEnd("down").fusedExonUp(8).reported(true).build()
        Assert.assertTrue(fusionEvidence.findMatches(wrongExon).isEmpty())
        val wrongGene: LinxFusion = TestLinxFactory.fusionBuilder().geneStart("down").geneEnd("up").fusedExonUp(5).reported(true).build()
        Assert.assertTrue(fusionEvidence.findMatches(wrongGene).isEmpty())
    }
}