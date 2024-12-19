package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeTrialFactory
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val FUSION_EVIDENCE_FOR_GENE = TestServeEvidenceFactory.createEvidenceForGene(gene = "gene 1", geneEvent = GeneEvent.FUSION)
private val DELETION_EVIDENCE_FOR_GENE = TestServeEvidenceFactory.createEvidenceForGene(gene = "gene 1", geneEvent = GeneEvent.DELETION)
private val ANY_EVIDENCE_FOR_GENE = TestServeEvidenceFactory.createEvidenceForGene(gene = "gene 1", geneEvent = GeneEvent.ANY_MUTATION)
private val SPECIFIC_FUSION_EVIDENCE =
    TestServeEvidenceFactory.createEvidenceForFusion(geneUp = "gene 1", geneDown = "gene 2", minExonUp = 4, maxExonUp = 6)
private val OTHER_FUSION_EVIDENCE = TestServeEvidenceFactory.createEvidenceForFusion(geneUp = "other gene 1", geneDown = "other gene 2")
private val OTHER_EVIDENCE = TestServeEvidenceFactory.createEvidenceForHla()

private val FUSION_TRIAL_FOR_GENE = TestServeTrialFactory.createTrialForGene(gene = "gene 1", geneEvent = GeneEvent.FUSION)
private val DELETION_TRIAL_FOR_GENE = TestServeTrialFactory.createTrialForGene(gene = "gene 1", geneEvent = GeneEvent.DELETION)
private val ANY_TRIAL_FOR_GENE = TestServeTrialFactory.createTrialForGene(gene = "gene 1", geneEvent = GeneEvent.ANY_MUTATION)
private val SPECIFIC_FUSION_TRIAL =
    TestServeTrialFactory.createTrialForFusion(geneUp = "gene 1", geneDown = "gene 2", minExonUp = 4, maxExonUp = 6)
private val OTHER_FUSION_TRIAL = TestServeTrialFactory.createTrialForFusion(geneUp = "other gene 1", geneDown = "other gene 2")
private val OTHER_TRIAL = TestServeTrialFactory.createTrialForHla()

class FusionEvidenceTest {

    private val fusionEvidence = FusionEvidence.create(
        evidences = listOf(
            FUSION_EVIDENCE_FOR_GENE,
            DELETION_EVIDENCE_FOR_GENE,
            ANY_EVIDENCE_FOR_GENE,
            SPECIFIC_FUSION_EVIDENCE,
            OTHER_FUSION_EVIDENCE,
            OTHER_EVIDENCE
        ),
        trials = listOf(
            FUSION_TRIAL_FOR_GENE,
            DELETION_TRIAL_FOR_GENE,
            ANY_TRIAL_FOR_GENE,
            SPECIFIC_FUSION_TRIAL,
            OTHER_FUSION_TRIAL,
            OTHER_TRIAL
        )
    )

    private val matchingFusion = FusionMatchCriteria(
        isReportable = true,
        geneStart = "gene 1",
        geneEnd = "gene 2",
        fusedExonUp = 5,
        fusedExonDown = 8,
        driverType = FusionDriverType.KNOWN_PAIR
    )

    @Test
    fun `Should determine evidence and trials for exact fusion match`() {
        val matches = fusionEvidence.findMatches(matchingFusion)
        assertThat(matches.evidenceMatches).containsExactlyInAnyOrder(
            FUSION_EVIDENCE_FOR_GENE,
            ANY_EVIDENCE_FOR_GENE,
            SPECIFIC_FUSION_EVIDENCE
        )

        assertThat(matches.matchingCriteriaPerTrialMatch).isEqualTo(
            mapOf(
                FUSION_TRIAL_FOR_GENE to FUSION_TRIAL_FOR_GENE.anyMolecularCriteria(),
                ANY_TRIAL_FOR_GENE to ANY_TRIAL_FOR_GENE.anyMolecularCriteria(),
                SPECIFIC_FUSION_TRIAL to SPECIFIC_FUSION_TRIAL.anyMolecularCriteria()
            )
        )
    }

    @Test
    fun `Should find no evidence or trial for unreportable fusion`() {
        val nonReportable = matchingFusion.copy(isReportable = false)

        val matches = fusionEvidence.findMatches(nonReportable)
        assertThat(matches.evidenceMatches).isEmpty()
        assertThat(matches.matchingCriteriaPerTrialMatch).isEmpty()
    }

    @Test
    fun `Should find evidence and trials for relevant promiscuous fusions`() {
        val otherGeneIsPromiscuous =
            matchingFusion.copy(geneStart = "other gene", geneEnd = "gene 1", driverType = FusionDriverType.PROMISCUOUS_5)

        val otherMatches = fusionEvidence.findMatches(otherGeneIsPromiscuous)
        assertThat(otherMatches.evidenceMatches).isEmpty()
        assertThat(otherMatches.matchingCriteriaPerTrialMatch).isEmpty()

        val correctGeneIsPromiscuous =
            matchingFusion.copy(geneStart = "other gene", geneEnd = "gene 1", driverType = FusionDriverType.PROMISCUOUS_3)

        val correctMatches = fusionEvidence.findMatches(correctGeneIsPromiscuous)
        assertThat(correctMatches.evidenceMatches).containsExactlyInAnyOrder(
            FUSION_EVIDENCE_FOR_GENE,
            ANY_EVIDENCE_FOR_GENE
        )

        assertThat(correctMatches.matchingCriteriaPerTrialMatch).isEqualTo(
            mapOf(
                FUSION_TRIAL_FOR_GENE to FUSION_TRIAL_FOR_GENE.anyMolecularCriteria(),
                ANY_TRIAL_FOR_GENE to ANY_TRIAL_FOR_GENE.anyMolecularCriteria()
            )
        )
    }
}