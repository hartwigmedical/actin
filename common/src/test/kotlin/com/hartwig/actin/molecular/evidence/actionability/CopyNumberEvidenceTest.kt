package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
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
    fun `Should determine evidence and trials for deletions`() {
        val del = create("gene 1", CopyNumberType.DEL)

        val matches = copyNumberEvidence.findMatches(del)
        assertThat(matches.evidenceMatches).containsExactly(DEL_EVIDENCE_FOR_GENE)
        assertThat(matches.matchingCriteriaPerTrialMatch).isEqualTo(mapOf(DEL_TRIAL_FOR_GENE to DEL_TRIAL_FOR_GENE.anyMolecularCriteria()))
    }

    @Test
    fun `Should not match evidence and trials for different gene`() {
        val amplificationOnOtherGene = create("other gene", CopyNumberType.FULL_GAIN)

        val ampMatches = copyNumberEvidence.findMatches(amplificationOnOtherGene)
        assertThat(ampMatches.evidenceMatches).isEmpty()
        assertThat(ampMatches.matchingCriteriaPerTrialMatch).isEmpty()

        val DelOnOtherGene = create("other gene", CopyNumberType.DEL)

        val delMatches = copyNumberEvidence.findMatches(DelOnOtherGene)
        assertThat(delMatches.evidenceMatches).isEmpty()
        assertThat(delMatches.matchingCriteriaPerTrialMatch).isEmpty()
    }

    @Test
    fun `Should not match evidence and trials for no amp or deletion`() {
        val noAmpOrDel = create("gene 1", CopyNumberType.NONE)

        val matches = copyNumberEvidence.findMatches(noAmpOrDel)
        assertThat(matches.evidenceMatches).isEmpty()
        assertThat(matches.matchingCriteriaPerTrialMatch).isEmpty()
    }

    private fun create(gene: String, copyNumberType: CopyNumberType): CopyNumber {
        return TestMolecularFactory.minimalCopyNumber().copy(
            gene = gene,
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(copyNumberType)
        )
    }
}