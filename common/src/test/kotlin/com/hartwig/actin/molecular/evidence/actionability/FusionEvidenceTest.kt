package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType.PROMISCUOUS_3
import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType.PROMISCUOUS_5
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.matching.FUSION_CRITERIA
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FusionEvidenceTest {

    @Test
    fun `Should determine promiscuous fusion evidence`() {
        val gene1 = TestServeEvidenceFactory.createEvidenceForGene(GeneEvent.FUSION, "gene 1")
        val gene2 = TestServeEvidenceFactory.createEvidenceForGene(GeneEvent.ANY_MUTATION, "gene 2")
        val gene3 = TestServeEvidenceFactory.createEvidenceForGene(GeneEvent.INACTIVATION, "gene 1")
        val fusionEvidence = FusionEvidence.create(evidences = listOf(gene1, gene2, gene3), trials = emptyList())

        val reportedFusionGene1 = FUSION_CRITERIA.copy(geneStart = "gene 1", driverType = PROMISCUOUS_5, isReportable = true)
        val evidenceMatchGene1 = fusionEvidence.findMatches(reportedFusionGene1)
        assertThat(evidenceMatchGene1.evidenceMatches.size).isEqualTo(1)
        assertThat(evidenceMatchGene1.evidenceMatches).contains(gene1)

        val unreportedFusionGene1 = reportedFusionGene1.copy(isReportable = false)
        assertThat(fusionEvidence.findMatches(unreportedFusionGene1).evidenceMatches).isEmpty()

        val wrongTypeFusionGene1 = reportedFusionGene1.copy(driverType = PROMISCUOUS_3)
        assertThat(fusionEvidence.findMatches(wrongTypeFusionGene1).evidenceMatches).isEmpty()

        val reportedFusionGene2 = FUSION_CRITERIA.copy(geneEnd = "gene 2", driverType = PROMISCUOUS_3, isReportable = true)
        val evidenceMatchGene2 = fusionEvidence.findMatches(reportedFusionGene2)
        assertThat(evidenceMatchGene2.evidenceMatches.size).isEqualTo(1)
        assertThat(evidenceMatchGene2.evidenceMatches).contains(gene2)
    }

    @Test
    fun `Should determine evidence for known fusions`() {
        val actionableFusion = TestServeEvidenceFactory.create(TestServeMolecularFactory.createFusion("up", "down", 4, 6))
        val fusionEvidence = FusionEvidence.create(evidences = listOf(actionableFusion), trials = emptyList())

        val match = FUSION_CRITERIA.copy(geneStart = "up", geneEnd = "down", fusedExonUp = 5, isReportable = true)
        val evidences = fusionEvidence.findMatches(match)
        assertThat(evidences.evidenceMatches.size).isEqualTo(1)
        assertThat(evidences.evidenceMatches).contains(actionableFusion)

        val notReported = match.copy(isReportable = false)
        assertThat(fusionEvidence.findMatches(notReported).evidenceMatches).isEmpty()

        val wrongExon = match.copy(fusedExonUp = 8)
        assertThat(fusionEvidence.findMatches(wrongExon).evidenceMatches).isEmpty()

        val wrongGene = match.copy(geneStart = "down", geneEnd = "up")
        assertThat(fusionEvidence.findMatches(wrongGene).evidenceMatches).isEmpty()
    }
}