package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.TestMolecularFactory.minimalCopyNumber
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumberType
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CopyNumberEvidenceTest {

    @Test
    fun `Should determine copy number evidence`() {
        val gene1: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.AMPLIFICATION).gene("gene 1").build()
        val gene2: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.DELETION).gene("gene 2").build()
        val gene3: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.INACTIVATION).gene("gene 1").build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().genes(listOf(gene1, gene2, gene3)).build()
        val copyNumberEvidence: CopyNumberEvidence = CopyNumberEvidence.create(actionable)

        val ampGene1 = minimalCopyNumber().copy(gene = "gene 1", type = CopyNumberType.FULL_GAIN)
        val ampMatches = copyNumberEvidence.findMatches(ampGene1)
        assertThat(ampMatches.size).isEqualTo(1)
        assertThat(ampMatches).contains(gene1)

        val lossGene2 = minimalCopyNumber().copy(gene = "gene 2", type = CopyNumberType.LOSS)
        val delMatches = copyNumberEvidence.findMatches(lossGene2)
        assertThat(delMatches.size).isEqualTo(1)
        assertThat(delMatches).contains(gene2)

        val lossGene1 = minimalCopyNumber().copy(gene = "gene 1", type = CopyNumberType.LOSS)
        assertThat(copyNumberEvidence.findMatches(lossGene1)).isEmpty()
    }
}