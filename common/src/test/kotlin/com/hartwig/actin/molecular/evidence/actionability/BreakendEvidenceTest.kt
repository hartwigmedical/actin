package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.minimalDisruption
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BreakendEvidenceTest {

    @Test
    fun `Should determine breakend evidence`() {
        val gene1: EfficacyEvidence = TestServeActionabilityFactory.createEvidenceForGene(GeneEvent.ANY_MUTATION, "gene 1")
        val gene2: EfficacyEvidence = TestServeActionabilityFactory.createEvidenceForGene(GeneEvent.AMPLIFICATION, "gene 2")
        val gene3: EfficacyEvidence = TestServeActionabilityFactory.createEvidenceForGene(GeneEvent.INACTIVATION, "gene 1")
        val actionableEvents = ActionableEvents(listOf(gene1, gene2, gene3), emptyList())
        val breakendEvidence: BreakendEvidence = BreakendEvidence.create(actionableEvents)

        val disruption = minimalDisruption().copy(gene = "gene 1", isReportable = true)
        val evidencesMatch = breakendEvidence.findMatches(disruption)
        assertThat(evidencesMatch.evidences.size).isEqualTo(1)
        assertThat(evidencesMatch.evidences).contains(gene1)

        val notReportedDisruption = disruption.copy(isReportable = false)
        assertThat(breakendEvidence.findMatches(notReportedDisruption).evidences).isEmpty()

        val wrongEventDisruption = disruption.copy(gene = "gene 2")
        assertThat(breakendEvidence.findMatches(wrongEventDisruption).evidences).isEmpty()
    }
}