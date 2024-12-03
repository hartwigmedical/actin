package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.minimalDisruption
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BreakendEvidenceTest {

    @Test
    fun `Should determine breakend evidence`() {
        val gene1 = TestServeEvidenceFactory.createEvidenceForGene(GeneEvent.ANY_MUTATION, "gene 1")
        val gene2 = TestServeEvidenceFactory.createEvidenceForGene(GeneEvent.AMPLIFICATION, "gene 2")
        val gene3 = TestServeEvidenceFactory.createEvidenceForGene(GeneEvent.INACTIVATION, "gene 1")
        val breakendEvidence = BreakendEvidence.create(evidences = listOf(gene1, gene2, gene3), trials = emptyList())

        val disruption = minimalDisruption().copy(gene = "gene 1", isReportable = true)
        val evidencesMatch = breakendEvidence.findMatches(disruption)
        assertThat(evidencesMatch.evidenceMatches.size).isEqualTo(1)
        assertThat(evidencesMatch.evidenceMatches).contains(gene1)

        val notReportedDisruption = disruption.copy(isReportable = false)
        assertThat(breakendEvidence.findMatches(notReportedDisruption).evidenceMatches).isEmpty()

        val wrongEventDisruption = disruption.copy(gene = "gene 2")
        assertThat(breakendEvidence.findMatches(wrongEventDisruption).evidenceMatches).isEmpty()
    }
}