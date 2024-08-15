package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.TestMolecularFactory.minimalHomozygousDisruption
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HomozygousDisruptionEvidenceTest {

    @Test
    fun `Should determine homozygous disruption evidence`() {
        val gene1: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.DELETION).gene("gene 1").build()
        val gene2: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.INACTIVATION).gene("gene 2").build()
        val gene3: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.AMPLIFICATION).gene("gene 3").build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().genes(listOf(gene1, gene2, gene3)).build()
        val homozygousDisruptionEvidence: HomozygousDisruptionEvidence = HomozygousDisruptionEvidence.create(actionable)

        val disruption1 = minimalHomozygousDisruption().copy(gene = "gene 1")
        val matchGene1 = homozygousDisruptionEvidence.findMatches(disruption1)
        assertThat(matchGene1.size).isEqualTo(1)
        assertThat(matchGene1).contains(gene1)

        val nonMatchDisruption = minimalHomozygousDisruption().copy(gene = "gene 3")
        assertThat(homozygousDisruptionEvidence.findMatches(nonMatchDisruption)).isEmpty()
    }
}