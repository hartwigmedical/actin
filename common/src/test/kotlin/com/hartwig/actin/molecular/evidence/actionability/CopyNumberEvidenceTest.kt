package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.minimalCopyNumber
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CopyNumberEvidenceTest {

    @Test
    fun `Should determine copy number evidence`() {
        val gene1 = TestServeEvidenceFactory.createEvidenceForGene(GeneEvent.AMPLIFICATION, "gene 1")
        val gene2 = TestServeEvidenceFactory.createEvidenceForGene(GeneEvent.DELETION, "gene 2")
        val gene3 = TestServeEvidenceFactory.createEvidenceForGene(GeneEvent.INACTIVATION, "gene 1")
        val copyNumberEvidence = CopyNumberEvidence.create(evidences = listOf(gene1, gene2, gene3), trials = emptyList())

        val ampGene1 = minimalCopyNumber().copy(
            gene = "gene 1",
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN)
        )
        val ampMatches = copyNumberEvidence.findMatches(ampGene1)
        assertThat(ampMatches.evidenceMatches.size).isEqualTo(1)
        assertThat(ampMatches.evidenceMatches).contains(gene1)

        val lossGene2 = minimalCopyNumber().copy(
            gene = "gene 2",
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.LOSS)
        )
        val delMatches = copyNumberEvidence.findMatches(lossGene2)
        assertThat(delMatches.evidenceMatches.size).isEqualTo(1)
        assertThat(delMatches.evidenceMatches).contains(gene2)

        val lossGene1 = minimalCopyNumber().copy(
            gene = "gene 1",
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.LOSS)
        )
        assertThat(copyNumberEvidence.findMatches(lossGene1).evidenceMatches).isEmpty()
    }
}