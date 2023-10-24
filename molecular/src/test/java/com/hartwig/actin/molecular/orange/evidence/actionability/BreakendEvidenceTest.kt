package com.hartwig.actin.molecular.orange.evidence.actionability

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent
import org.junit.Assert
import org.junit.Test

class BreakendEvidenceTest {
    @Test
    fun canDetermineBreakendEvidence() {
        val gene1: ActionableGene? = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.ANY_MUTATION).gene("gene 1").build()
        val gene2: ActionableGene? = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.AMPLIFICATION).gene("gene 2").build()
        val gene3: ActionableGene? = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.INACTIVATION).gene("gene 1").build()
        val actionable: ActionableEvents? = ImmutableActionableEvents.builder().genes(Lists.newArrayList(gene1, gene2, gene3)).build()
        val breakendEvidence: BreakendEvidence = BreakendEvidence.Companion.create(actionable)
        val evidencesMatch = breakendEvidence.findMatches(TestLinxFactory.breakendBuilder().gene("gene 1").reportedDisruption(true).build())
        Assert.assertEquals(1, evidencesMatch.size.toLong())
        Assert.assertTrue(evidencesMatch.contains(gene1))

        // Not reported
        Assert.assertTrue(breakendEvidence.findMatches(TestLinxFactory.breakendBuilder().gene("gene 1").reportedDisruption(false).build())
            .isEmpty())

        // Wrong event
        Assert.assertTrue(breakendEvidence.findMatches(TestLinxFactory.breakendBuilder().gene("gene 2").reportedDisruption(true).build())
            .isEmpty())
    }
}