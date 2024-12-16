package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType
import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType.NONE
import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType.PROMISCUOUS_3
import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType.PROMISCUOUS_5
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FusionEvidenceTest {

    @Test
    fun `Should determine promiscuous fusion evidence`() {
        val evidenceForGene1 = TestServeEvidenceFactory.createEvidenceForGene(gene = "gene 1", geneEvent = GeneEvent.FUSION)
        val evidenceForGene2 = TestServeEvidenceFactory.createEvidenceForGene(gene = "gene 2", geneEvent = GeneEvent.ANY_MUTATION)
        val evidenceForGene3 = TestServeEvidenceFactory.createEvidenceForGene(gene = "gene 1", geneEvent = GeneEvent.INACTIVATION)
        val fusionEvidence =
            FusionEvidence.create(evidences = listOf(evidenceForGene1, evidenceForGene2, evidenceForGene3), trials = emptyList())

        val reportedFusionGene1 = createFusionCriteria(isReportable = true, geneStart = "gene 1", driverType = PROMISCUOUS_5)
        val actionabilityMatchGene1 = fusionEvidence.findMatches(reportedFusionGene1)
        assertThat(actionabilityMatchGene1.evidenceMatches.size).isEqualTo(1)
        assertThat(actionabilityMatchGene1.evidenceMatches).containsExactly(evidenceForGene1)

        val unreportedFusionGene1 = createFusionCriteria(isReportable = false, geneStart = "gene 1", driverType = PROMISCUOUS_5)
        assertThat(fusionEvidence.findMatches(unreportedFusionGene1).evidenceMatches).isEmpty()

        val wrongTypeFusionGene1 = createFusionCriteria(isReportable = true, geneStart = "gene 1", driverType = PROMISCUOUS_3)
        assertThat(fusionEvidence.findMatches(wrongTypeFusionGene1).evidenceMatches).isEmpty()

        val reportedFusionGene2 = createFusionCriteria(isReportable = true, geneEnd = "gene 2", driverType = PROMISCUOUS_3)
        val evidenceMatchGene2 = fusionEvidence.findMatches(reportedFusionGene2)
        assertThat(evidenceMatchGene2.evidenceMatches.size).isEqualTo(1)
        assertThat(evidenceMatchGene2.evidenceMatches).containsExactly(evidenceForGene2)
    }

    @Test
    fun `Should determine evidence for known fusions`() {
        val actionableFusionEvidence =
            TestServeEvidenceFactory.create(
                molecularCriterium = TestServeMolecularFactory.createFusionCriterium(
                    geneUp = "up",
                    geneDown = "down",
                    minExonUp = 4,
                    maxExonUp = 6
                )
            )
        val fusionEvidence = FusionEvidence.create(evidences = listOf(actionableFusionEvidence), trials = emptyList())

        val match = createFusionCriteria(isReportable = true, geneStart = "up", geneEnd = "down", fusedExonUp = 5)
        val evidences = fusionEvidence.findMatches(match)
        assertThat(evidences.evidenceMatches.size).isEqualTo(1)
        assertThat(evidences.evidenceMatches).contains(actionableFusionEvidence)

        val notReported = match.copy(isReportable = false)
        assertThat(fusionEvidence.findMatches(notReported).evidenceMatches).isEmpty()

        val wrongExon = match.copy(fusedExonUp = 8)
        assertThat(fusionEvidence.findMatches(wrongExon).evidenceMatches).isEmpty()

        val wrongGene = match.copy(geneStart = "down", geneEnd = "up")
        assertThat(fusionEvidence.findMatches(wrongGene).evidenceMatches).isEmpty()
    }

    private fun createFusionCriteria(
        isReportable: Boolean,
        geneStart: String = "",
        geneEnd: String = "",
        fusedExonUp: Int? = null,
        fusedExonDown: Int? = null,
        driverType: FusionDriverType = NONE
    ): FusionMatchCriteria {
        return FusionMatchCriteria(
            isReportable = isReportable,
            geneStart = geneStart,
            geneEnd = geneEnd,
            fusedExonUp = fusedExonUp,
            fusedExonDown = fusedExonDown,
            driverType = driverType
        )
    }
}