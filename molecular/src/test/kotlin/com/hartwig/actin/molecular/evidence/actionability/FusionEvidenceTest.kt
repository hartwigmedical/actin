package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.datamodel.orange.driver.FusionDriverType.PROMISCUOUS_3
import com.hartwig.actin.molecular.datamodel.orange.driver.FusionDriverType.PROMISCUOUS_5
import com.hartwig.actin.molecular.evidence.matching.FUSION_CRITERIA
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.fusion.ActionableFusion
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FusionEvidenceTest {

    @Test
    fun `Should determine promiscuous fusion evidence`() {
        val gene1: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.FUSION).gene("gene 1").build()
        val gene2: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.ANY_MUTATION).gene("gene 2").build()
        val gene3: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.INACTIVATION).gene("gene 1").build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().genes(listOf(gene1, gene2, gene3)).build()
        val fusionEvidence: FusionEvidence = FusionEvidence.create(actionable)

        val reportedFusionGene1 = FUSION_CRITERIA.copy(geneStart = "gene 1", driverType = PROMISCUOUS_5, isReportable = true)
        val evidenceMatchGene1 = fusionEvidence.findMatches(reportedFusionGene1)
        assertThat(evidenceMatchGene1.size).isEqualTo(1)
        assertThat(evidenceMatchGene1).contains(gene1)

        val unreportedFusionGene1 = reportedFusionGene1.copy(isReportable = false)
        assertThat(fusionEvidence.findMatches(unreportedFusionGene1)).isEmpty()

        val wrongTypeFusionGene1 = reportedFusionGene1.copy(driverType = PROMISCUOUS_3)
        assertThat(fusionEvidence.findMatches(wrongTypeFusionGene1)).isEmpty()

        val reportedFusionGene2 = FUSION_CRITERIA.copy(geneEnd = "gene 2", driverType = PROMISCUOUS_3, isReportable = true)
        val evidenceMatchGene2 = fusionEvidence.findMatches(reportedFusionGene2)
        assertThat(evidenceMatchGene2.size).isEqualTo(1)
        assertThat(evidenceMatchGene2).contains(gene2)
    }

    @Test
    fun `Should determine evidence for known fusions`() {
        val actionableFusion: ActionableFusion =
            TestServeActionabilityFactory.fusionBuilder().geneUp("up").geneDown("down").minExonUp(4).maxExonUp(6).build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().addFusions(actionableFusion).build()
        val fusionEvidence: FusionEvidence = FusionEvidence.create(actionable)

        val match = FUSION_CRITERIA.copy(geneStart = "up", geneEnd = "down", fusedExonUp = 5, isReportable = true)
        val evidences = fusionEvidence.findMatches(match)
        assertThat(evidences.size).isEqualTo(1)
        assertThat(evidences).contains(actionableFusion)

        val notReported = match.copy(isReportable = false)
        assertThat(fusionEvidence.findMatches(notReported)).isEmpty()

        val wrongExon = match.copy(fusedExonUp = 8)
        assertThat(fusionEvidence.findMatches(wrongExon)).isEmpty()

        val wrongGene = match.copy(geneStart = "down", geneEnd = "up")
        assertThat(fusionEvidence.findMatches(wrongGene)).isEmpty()
    }
}