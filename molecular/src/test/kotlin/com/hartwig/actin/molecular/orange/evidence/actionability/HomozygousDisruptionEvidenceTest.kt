package com.hartwig.actin.molecular.orange.evidence.actionability

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory
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

        val matchGene1 = homozygousDisruptionEvidence.findMatches(TestLinxFactory.homozygousDisruptionBuilder().gene("gene 1").build())
        assertEquals(1, matchGene1.size.toLong())
        assertTrue(matchGene1.contains(gene1))

        val matchGene2 = homozygousDisruptionEvidence.findMatches(TestLinxFactory.homozygousDisruptionBuilder().gene("gene 2").build())
        assertEquals(1, matchGene2.size.toLong())
        assertTrue(matchGene2.contains(gene2))
        assertTrue(homozygousDisruptionEvidence.findMatches(TestLinxFactory.homozygousDisruptionBuilder().gene("gene 3").build()).isEmpty())
    }
}