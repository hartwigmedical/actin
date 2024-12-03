package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.minimalHomozygousDisruption
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HomozygousDisruptionEvidenceTest {

    @Test
    fun `Should determine homozygous disruption evidence`() {
        val gene1 = TestServeEvidenceFactory.createEvidenceForGene(GeneEvent.DELETION, "gene 1")
        val gene2 = TestServeEvidenceFactory.createEvidenceForGene(GeneEvent.INACTIVATION, "gene 2")
        val gene3 = TestServeEvidenceFactory.createEvidenceForGene(GeneEvent.AMPLIFICATION, "gene 3")
        val homozygousDisruptionEvidence =
            HomozygousDisruptionEvidence.create(evidences = listOf(gene1, gene2, gene3), trials = emptyList())

        val disruption1 = minimalHomozygousDisruption().copy(gene = "gene 1")
        val matchGene1 = homozygousDisruptionEvidence.findMatches(disruption1)
        assertThat(matchGene1.evidenceMatches.size).isEqualTo(1)
        assertThat(matchGene1.evidenceMatches).contains(gene1)

        val nonMatchDisruption = minimalHomozygousDisruption().copy(gene = "gene 3")
        assertThat(homozygousDisruptionEvidence.findMatches(nonMatchDisruption).evidenceMatches).isEmpty()
    }
}