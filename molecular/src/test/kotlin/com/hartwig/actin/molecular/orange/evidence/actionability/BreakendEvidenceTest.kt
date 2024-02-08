package com.hartwig.actin.molecular.orange.evidence.actionability

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.orange.evidence.TestMolecularFactory
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BreakendEvidenceTest {

    @Test
    fun canDetermineBreakendEvidence() {
        val gene1: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.ANY_MUTATION).gene("gene 1").build()
        val gene2: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.AMPLIFICATION).gene("gene 2").build()
        val gene3: ActionableGene = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.INACTIVATION).gene("gene 1").build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().genes(Lists.newArrayList(gene1, gene2, gene3)).build()
        val breakendEvidence: BreakendEvidence = BreakendEvidence.create(actionable)

//        val evidencesMatch = breakendEvidence.findMatches(TestLinxFactory.breakendBuilder().gene("gene 1").reported(true).build())
        val disruption = TestMolecularFactory.minimalTestDisruption().copy(gene = "gene 1", isReportable = true)
        val evidencesMatch = breakendEvidence.findMatches(disruption)
        assertEquals(1, evidencesMatch.size.toLong())
        assertTrue(evidencesMatch.contains(gene1))

        // Not reported
//        assertTrue(breakendEvidence.findMatches(TestLinxFactory.breakendBuilder().gene("gene 1").reported(false).build()).isEmpty())
        val notReportedDisruption = disruption.copy(isReportable = false)
        assertTrue(breakendEvidence.findMatches(notReportedDisruption).isEmpty())

        // Wrong event
        val wrongEventDisruption = disruption.copy(gene = "gene 2")
//        assertTrue(breakendEvidence.findMatches(TestLinxFactory.breakendBuilder().gene("gene 2").reported(true).build()).isEmpty())
        assertTrue(breakendEvidence.findMatches(wrongEventDisruption).isEmpty())
    }
}