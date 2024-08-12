package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.serve.TestServeActionabilityFactory
import com.hartwig.actin.molecular.TestMolecularFactory.minimalDisruption
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BreakendEvidenceTest {

    @Test
    fun `Should determine breakend evidence`() {
        val gene1: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.ANY_MUTATION).gene("gene 1").build()
        val gene2: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.AMPLIFICATION).gene("gene 2").build()
        val gene3: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.INACTIVATION).gene("gene 1").build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().genes(listOf(gene1, gene2, gene3)).build()
        val breakendEvidence: BreakendEvidence = BreakendEvidence.create(actionable)

        val disruption = minimalDisruption().copy(gene = "gene 1", isReportable = true)
        val evidencesMatch = breakendEvidence.findMatches(disruption)
        assertThat(evidencesMatch.size).isEqualTo(1)
        assertThat(evidencesMatch).contains(gene1)

        // Not reported
        val notReportedDisruption = disruption.copy(isReportable = false)
        assertThat(breakendEvidence.findMatches(notReportedDisruption)).isEmpty()

        // Wrong event
        val wrongEventDisruption = disruption.copy(gene = "gene 2")
        assertThat(breakendEvidence.findMatches(wrongEventDisruption)).isEmpty()
    }
}