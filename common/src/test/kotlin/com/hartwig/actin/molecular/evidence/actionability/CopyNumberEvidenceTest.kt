package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeTrialFactory
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val AMP_EVIDENCE_FOR_GENE = TestServeEvidenceFactory.createEvidenceForGene(gene = "gene 1", geneEvent = GeneEvent.AMPLIFICATION)
private val DEL_EVIDENCE_FOR_GENE = TestServeEvidenceFactory.createEvidenceForGene(gene = "gene 1", geneEvent = GeneEvent.DELETION)
private val INACT_EVIDENCE_FOR_GENE = TestServeEvidenceFactory.createEvidenceForGene(gene = "gene 1", geneEvent = GeneEvent.INACTIVATION)
private val OTHER_EVIDENCE = TestServeEvidenceFactory.createEvidenceForHla()

private val AMP_TRIAL_FOR_GENE = TestServeTrialFactory.createTrialForGene(gene = "gene 1", geneEvent = GeneEvent.AMPLIFICATION)
private val DEL_TRIAL_FOR_GENE = TestServeTrialFactory.createTrialForGene(gene = "gene 1", geneEvent = GeneEvent.DELETION)
private val INACT_TRIAL_FOR_GENE = TestServeTrialFactory.createTrialForGene(gene = "gene 1", geneEvent = GeneEvent.INACTIVATION)
private val OTHER_TRIAL = TestServeTrialFactory.createTrialForHla()

class CopyNumberEvidenceTest {

    private val copyNumberEvidence = CopyNumberEvidence.create(
        evidences = listOf(AMP_EVIDENCE_FOR_GENE, DEL_EVIDENCE_FOR_GENE, INACT_EVIDENCE_FOR_GENE, OTHER_EVIDENCE),
        trials = listOf(AMP_TRIAL_FOR_GENE, DEL_TRIAL_FOR_GENE, INACT_TRIAL_FOR_GENE, OTHER_TRIAL)
    )

    @Test
    fun `Should determine evidence and trials for amplifications`() {
        val amplification = create("gene 1", CopyNumberType.FULL_GAIN)

        val matches = copyNumberEvidence.findMatches(amplification)
        assertThat(matches.evidenceMatches).containsExactly(AMP_EVIDENCE_FOR_GENE)
        assertThat(matches.matchingCriteriaPerTrialMatch).isEqualTo(mapOf(AMP_TRIAL_FOR_GENE to AMP_TRIAL_FOR_GENE.anyMolecularCriteria()))
    }

    @Test
    fun `Should determine evidence and trials for losses`() {
        val loss = create("gene 1", CopyNumberType.LOSS)

        val matches = copyNumberEvidence.findMatches(loss)
        assertThat(matches.evidenceMatches).containsExactly(DEL_EVIDENCE_FOR_GENE)
        assertThat(matches.matchingCriteriaPerTrialMatch).isEqualTo(mapOf(DEL_TRIAL_FOR_GENE to DEL_TRIAL_FOR_GENE.anyMolecularCriteria()))
    }

    @Test
    fun `Should not match evidence and trials for different gene`() {
        val amplificationOnOtherGene = create("other gene", CopyNumberType.FULL_GAIN)

        val ampMatches = copyNumberEvidence.findMatches(amplificationOnOtherGene)
        assertThat(ampMatches.evidenceMatches).isEmpty()
        assertThat(ampMatches.matchingCriteriaPerTrialMatch).isEmpty()

        val lossOnOtherGene = create("other gene", CopyNumberType.LOSS)

        val lossMatches = copyNumberEvidence.findMatches(lossOnOtherGene)
        assertThat(lossMatches.evidenceMatches).isEmpty()
        assertThat(lossMatches.matchingCriteriaPerTrialMatch).isEmpty()
    }

    private fun create(gene: String, copyNumberType: CopyNumberType): CopyNumber {
        return TestMolecularFactory.minimalCopyNumber().copy(
            gene = gene,
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(copyNumberType)
        )
    }
}