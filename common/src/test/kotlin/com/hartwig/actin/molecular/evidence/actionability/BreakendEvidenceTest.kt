package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.orange.driver.Disruption
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

class BreakendEvidenceTest {

    private val breakendEvidence = BreakendEvidence.create(
        evidences = listOf(ANY_EVIDENCE_FOR_GENE, AMP_EVIDENCE_FOR_GENE, INACT_EVIDENCE_FOR_GENE, OTHER_EVIDENCE),
        trials = listOf(ANY_TRIAL_FOR_GENE, AMP_TRIAL_FOR_GENE, INACT_TRIAL_FOR_GENE, OTHER_TRIAL)
    )

    @Test
    fun `Should determine evidence and trials for matching disruption`() {
        val disruption = create(gene = "gene 1", isReportable = true)

        val matches = breakendEvidence.findMatches(disruption)
        assertThat(matches.evidenceMatches).containsExactly(ANY_EVIDENCE_FOR_GENE)
        assertThat(matches.matchingCriteriaPerTrialMatch).isEqualTo(mapOf(ANY_TRIAL_FOR_GENE to ANY_TRIAL_FOR_GENE.anyMolecularCriteria()))
    }

    @Test
    fun `Should not match evidence to trials to unreportable disruption`() {
        val disruption = create(gene = "gene 1", isReportable = false)

        val matches = breakendEvidence.findMatches(disruption)
        assertThat(matches.evidenceMatches).isEmpty()
        assertThat(matches.matchingCriteriaPerTrialMatch).isEmpty()
    }

    @Test
    fun `Should not match evidence to trials to disruption on other gene`() {
        val disruption = create(gene = "other gene", isReportable = true)

        val matches = breakendEvidence.findMatches(disruption)
        assertThat(matches.evidenceMatches).isEmpty()
        assertThat(matches.matchingCriteriaPerTrialMatch).isEmpty()
    }

    private fun create(gene: String, isReportable: Boolean): Disruption {
        return TestMolecularFactory.minimalDisruption().copy(gene = gene, isReportable = isReportable)
    }
}