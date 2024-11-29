package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType.PROMISCUOUS_3
import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType.PROMISCUOUS_5
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.actin.molecular.evidence.matching.FUSION_CRITERIA
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FusionEvidenceTest {

    @Test
    fun `Should determine promiscuous fusion evidence`() {
        val gene1: EfficacyEvidence = TestServeActionabilityFactory.createEfficacyEvidenceWithGene(GeneEvent.FUSION, "gene 1")
        val gene2: EfficacyEvidence = TestServeActionabilityFactory.createEfficacyEvidenceWithGene(GeneEvent.ANY_MUTATION, "gene 2")
        val gene3: EfficacyEvidence = TestServeActionabilityFactory.createEfficacyEvidenceWithGene(GeneEvent.INACTIVATION, "gene 1")
        val actionableEvents = ActionableEvents(listOf(gene1, gene2, gene3), emptyList())
        val fusionEvidence: FusionEvidence = FusionEvidence.create(actionableEvents)

        val reportedFusionGene1 = FUSION_CRITERIA.copy(geneStart = "gene 1", driverType = PROMISCUOUS_5, isReportable = true)
        val evidenceMatchGene1 = fusionEvidence.findMatches(reportedFusionGene1)
        assertThat(evidenceMatchGene1.evidences.size).isEqualTo(1)
        assertThat(evidenceMatchGene1.evidences).contains(gene1)

        val unreportedFusionGene1 = reportedFusionGene1.copy(isReportable = false)
        assertThat(fusionEvidence.findMatches(unreportedFusionGene1).evidences).isEmpty()

        val wrongTypeFusionGene1 = reportedFusionGene1.copy(driverType = PROMISCUOUS_3)
        assertThat(fusionEvidence.findMatches(wrongTypeFusionGene1).evidences).isEmpty()

        val reportedFusionGene2 = FUSION_CRITERIA.copy(geneEnd = "gene 2", driverType = PROMISCUOUS_3, isReportable = true)
        val evidenceMatchGene2 = fusionEvidence.findMatches(reportedFusionGene2)
        assertThat(evidenceMatchGene2.evidences.size).isEqualTo(1)
        assertThat(evidenceMatchGene2.evidences).contains(gene2)
    }

    @Test
    fun `Should determine evidence for known fusions`() {
        val actionableFusion: EfficacyEvidence =
            TestServeActionabilityFactory.createEfficacyEvidence(TestServeActionabilityFactory.createFusion("up", "down", 4, 6))
        val actionableEvents = ActionableEvents(listOf(actionableFusion), emptyList())
        val fusionEvidence: FusionEvidence = FusionEvidence.create(actionableEvents)

        val match = FUSION_CRITERIA.copy(geneStart = "up", geneEnd = "down", fusedExonUp = 5, isReportable = true)
        val evidences = fusionEvidence.findMatches(match)
        assertThat(evidences.evidences.size).isEqualTo(1)
        assertThat(evidences.evidences).contains(actionableFusion)

        val notReported = match.copy(isReportable = false)
        assertThat(fusionEvidence.findMatches(notReported).evidences).isEmpty()

        val wrongExon = match.copy(fusedExonUp = 8)
        assertThat(fusionEvidence.findMatches(wrongExon).evidences).isEmpty()

        val wrongGene = match.copy(geneStart = "down", geneEnd = "up")
        assertThat(fusionEvidence.findMatches(wrongGene).evidences).isEmpty()
    }
}