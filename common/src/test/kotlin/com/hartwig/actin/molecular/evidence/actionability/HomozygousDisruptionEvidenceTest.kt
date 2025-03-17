package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.TestServeTrialFactory
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val ANY_EVIDENCE_FOR_GENE = TestServeEvidenceFactory.createEvidenceForGene(gene = "gene 1", geneEvent = GeneEvent.ANY_MUTATION)
private val AMP_EVIDENCE_FOR_GENE = TestServeEvidenceFactory.createEvidenceForGene(gene = "gene 1", geneEvent = GeneEvent.AMPLIFICATION)
private val INACT_EVIDENCE_FOR_GENE = TestServeEvidenceFactory.createEvidenceForGene(gene = "gene 1", geneEvent = GeneEvent.INACTIVATION)
private val OTHER_EVIDENCE =
    TestServeEvidenceFactory.createEvidenceForHotspot(TestServeMolecularFactory.createVariantAnnotation(gene = "gene 1"))

private val ANY_TRIAL_FOR_GENE = TestServeTrialFactory.createTrialForGene(gene = "gene 1", geneEvent = GeneEvent.ANY_MUTATION)
private val AMP_TRIAL_FOR_GENE = TestServeTrialFactory.createTrialForGene(gene = "gene 1", geneEvent = GeneEvent.AMPLIFICATION)
private val INACT_TRIAL_FOR_GENE = TestServeTrialFactory.createTrialForGene(gene = "gene 1", geneEvent = GeneEvent.INACTIVATION)
private val OTHER_TRIAL =
    TestServeTrialFactory.createTrialForHotspot(TestServeMolecularFactory.createVariantAnnotation(gene = "gene 1"))

class HomozygousDisruptionEvidenceTest {

    private val homozygousDisruptionEvidence = HomozygousDisruptionEvidence.create(
        evidences = listOf(ANY_EVIDENCE_FOR_GENE, AMP_EVIDENCE_FOR_GENE, INACT_EVIDENCE_FOR_GENE, OTHER_EVIDENCE),
        trials = listOf(ANY_TRIAL_FOR_GENE, AMP_TRIAL_FOR_GENE, INACT_TRIAL_FOR_GENE, OTHER_TRIAL)
    )

    @Test
    fun `Should determine evidence and trials for matching homozygous disruption`() {
        val homozygousDisruption = TestMolecularFactory.minimalHomozygousDisruption().copy(gene = "gene 1")

        val matches = homozygousDisruptionEvidence.findMatches(homozygousDisruption)
        assertThat(matches.evidenceMatches).containsExactly(ANY_EVIDENCE_FOR_GENE, INACT_EVIDENCE_FOR_GENE)
        assertThat(matches.matchingCriteriaPerTrialMatch).isEqualTo(
            mapOf(
                ANY_TRIAL_FOR_GENE to ANY_TRIAL_FOR_GENE.anyMolecularCriteria(),
                INACT_TRIAL_FOR_GENE to INACT_TRIAL_FOR_GENE.anyMolecularCriteria()
            )
        )
    }

    @Test
    fun `Should not match evidence and trials to homozygous disruption on different gene`() {
        val nonMatchDisruption = TestMolecularFactory.minimalHomozygousDisruption().copy(gene = "gene 2")

        val matches = homozygousDisruptionEvidence.findMatches(nonMatchDisruption)
        assertThat(matches.evidenceMatches).isEmpty()
        assertThat(matches.matchingCriteriaPerTrialMatch).isEmpty()
    }
}