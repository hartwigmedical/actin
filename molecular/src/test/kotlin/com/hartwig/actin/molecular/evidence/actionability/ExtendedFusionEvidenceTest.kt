package com.hartwig.actin.molecular.evidence.actionability

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.datamodel.hmf.driver.FusionDriverType.PROMISCUOUS_3
import com.hartwig.actin.molecular.datamodel.hmf.driver.FusionDriverType.PROMISCUOUS_5
import com.hartwig.actin.molecular.evidence.matching.FUSION_CRITERIA
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.fusion.ActionableFusion
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtendedFusionEvidenceTest {

    @Test
    fun `Should determine promiscuous fusion evidence`() {
        val gene1: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.FUSION).gene("gene 1").build()
        val gene2: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.ANY_MUTATION).gene("gene 2").build()
        val gene3: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.INACTIVATION).gene("gene 1").build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().genes(Lists.newArrayList(gene1, gene2, gene3)).build()
        val fusionEvidence: FusionEvidence = FusionEvidence.create(actionable)

        val reportedFusionGene1 = FUSION_CRITERIA.copy(geneStart = "gene 1", driverType = PROMISCUOUS_5, isReportable = true)
        val evidenceMatchGene1 = fusionEvidence.findMatches(reportedFusionGene1)
        assertEquals(1, evidenceMatchGene1.size.toLong())
        assertTrue(evidenceMatchGene1.contains(gene1))

        val unreportedFusionGene1 = reportedFusionGene1.copy(isReportable = false)
        assertTrue(fusionEvidence.findMatches(unreportedFusionGene1).isEmpty())

        val wrongTypeFusionGene1 = reportedFusionGene1.copy(driverType = PROMISCUOUS_3)
        assertTrue(fusionEvidence.findMatches(wrongTypeFusionGene1).isEmpty())

        val reportedFusionGene2 = FUSION_CRITERIA.copy(geneEnd = "gene 2", driverType = PROMISCUOUS_3, isReportable = true)
        val evidenceMatchGene2 = fusionEvidence.findMatches(reportedFusionGene2)
        assertEquals(1, evidenceMatchGene2.size.toLong())
        assertTrue(evidenceMatchGene2.contains(gene2))
    }

    @Test
    fun `Should determine evidence for known fusions`() {
        val actionableFusion: ActionableFusion =
            TestServeActionabilityFactory.fusionBuilder().geneUp("up").geneDown("down").minExonUp(4).maxExonUp(6).build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().addFusions(actionableFusion).build()
        val fusionEvidence: FusionEvidence = FusionEvidence.create(actionable)

        val match = FUSION_CRITERIA.copy(geneStart = "up", geneEnd = "down", fusedExonUp = 5, isReportable = true)
        val evidences = fusionEvidence.findMatches(match)
        assertEquals(1, evidences.size.toLong())
        assertTrue(evidences.contains(actionableFusion))

        val notReported = match.copy(isReportable = false)
        assertTrue(fusionEvidence.findMatches(notReported).isEmpty())

        val wrongExon = match.copy(fusedExonUp = 8)
        assertTrue(fusionEvidence.findMatches(wrongExon).isEmpty())

        val wrongGene = match.copy(geneStart = "down", geneEnd = "up")
        assertTrue(fusionEvidence.findMatches(wrongGene).isEmpty())
    }
}