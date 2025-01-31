package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeTrialFactory
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val ANY_EVIDENCE_FOR_GENE = TestServeEvidenceFactory.createEvidenceForGene(gene = "gene 1", geneEvent = GeneEvent.ANY_MUTATION)
private val AMP_EVIDENCE_FOR_GENE = TestServeEvidenceFactory.createEvidenceForGene(gene = "gene 1", geneEvent = GeneEvent.AMPLIFICATION)
private val INACT_EVIDENCE_FOR_GENE = TestServeEvidenceFactory.createEvidenceForGene(gene = "gene 1", geneEvent = GeneEvent.INACTIVATION)
private val OTHER_EVIDENCE = TestServeEvidenceFactory.createEvidenceForHotspot()

private val ANY_TRIAL_FOR_GENE = TestServeTrialFactory.createTrialForGene(gene = "gene 1", geneEvent = GeneEvent.ANY_MUTATION)
private val AMP_TRIAL_FOR_GENE = TestServeTrialFactory.createTrialForGene(gene = "gene 1", geneEvent = GeneEvent.AMPLIFICATION)
private val INACT_TRIAL_FOR_GENE = TestServeTrialFactory.createTrialForGene(gene = "gene 1", geneEvent = GeneEvent.INACTIVATION)
private val OTHER_TRIAL = TestServeTrialFactory.createTrialForHotspot()

class DisruptionEvidenceTest {

    private val matchingDisruption =
        TestMolecularFactory.minimalDisruption().copy(gene = "gene 1", isReportable = true)
    private val disruptionEvidence = DisruptionEvidence.create(
        evidences = listOf(ANY_EVIDENCE_FOR_GENE, AMP_EVIDENCE_FOR_GENE, INACT_EVIDENCE_FOR_GENE, OTHER_EVIDENCE),
        trials = listOf(ANY_TRIAL_FOR_GENE, AMP_TRIAL_FOR_GENE, INACT_TRIAL_FOR_GENE, OTHER_TRIAL)
    )

    @Test
    fun `Should determine evidence and trials for matching disruption`() {
        val matches = disruptionEvidence.findMatches(matchingDisruption)
        assertThat(matches.evidenceMatches).containsExactly(ANY_EVIDENCE_FOR_GENE)
        assertThat(matches.matchingCriteriaPerTrialMatch).isEqualTo(mapOf(ANY_TRIAL_FOR_GENE to ANY_TRIAL_FOR_GENE.anyMolecularCriteria()))
    }

    @Test
    fun `Should not match evidence and trials to disruption on other gene`() {
        val matches = disruptionEvidence.findMatches(matchingDisruption.copy(gene = "other gene"))
        assertThat(matches.evidenceMatches).isEmpty()
        assertThat(matches.matchingCriteriaPerTrialMatch).isEmpty()
    }

    @Test
    fun `Should not match evidence and trials to unreportable disruption`() {
        val matches = disruptionEvidence.findMatches(matchingDisruption.copy(isReportable = false))
        assertThat(matches.evidenceMatches).isEmpty()
        assertThat(matches.matchingCriteriaPerTrialMatch).isEmpty()
    }

    @Test
    fun `Should not match evidence and trials to disruption if gene role is TSG`() {
        val matches = disruptionEvidence.findMatches(matchingDisruption.copy(geneRole = GeneRole.TSG))
        assertThat(matches.evidenceMatches).isEmpty()
        assertThat(matches.matchingCriteriaPerTrialMatch).isEmpty()
    }
}