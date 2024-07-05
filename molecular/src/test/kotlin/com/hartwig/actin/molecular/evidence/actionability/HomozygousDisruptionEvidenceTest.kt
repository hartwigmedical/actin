package com.hartwig.actin.molecular.evidence.actionability

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.TestMolecularFactory.minimalHomozygousDisruption
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomozygousDisruptionEvidenceTest {

    @Test
    fun canDetermineHomozygousDisruptionEvidence() {
        val gene1: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.DELETION).gene("gene 1").build()
        val gene2: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.INACTIVATION).gene("gene 2").build()
        val gene3: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.AMPLIFICATION).gene("gene 3").build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().genes(Lists.newArrayList(gene1, gene2, gene3)).build()
        val homozygousDisruptionEvidence: HomozygousDisruptionEvidence = HomozygousDisruptionEvidence.create(actionable)

        val disruption1 = minimalHomozygousDisruption().copy(gene = "gene 1")
        val matchGene1 = homozygousDisruptionEvidence.findMatches(disruption1)
        assertEquals(1, matchGene1.size.toLong())
        assertTrue(matchGene1.contains(gene1))

        val nonMatchDisruption = minimalHomozygousDisruption().copy(gene = "gene 3")
        assertTrue(homozygousDisruptionEvidence.findMatches(nonMatchDisruption).isEmpty())
    }
}