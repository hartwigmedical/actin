package com.hartwig.actin.molecular.orange.evidence.actionability;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.ImmutableActionableEvents;
import com.hartwig.serve.datamodel.gene.ActionableGene;
import com.hartwig.serve.datamodel.gene.GeneEvent;

import org.junit.Test;

public class BreakendEvidenceTest {

    @Test
    public void canDetermineBreakendEvidence() {
        ActionableGene gene1 = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.ANY_MUTATION).gene("gene 1").build();
        ActionableGene gene2 = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.AMPLIFICATION).gene("gene 2").build();
        ActionableGene gene3 = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.INACTIVATION).gene("gene 1").build();

        ActionableEvents actionable = ImmutableActionableEvents.builder().genes(Lists.newArrayList(gene1, gene2, gene3)).build();

        BreakendEvidence breakendEvidence = BreakendEvidence.create(actionable);

        List<ActionableEvent> evidencesMatch =
                breakendEvidence.findMatches(TestLinxFactory.breakendBuilder().gene("gene 1").reported(true).build());
        assertEquals(1, evidencesMatch.size());
        assertTrue(evidencesMatch.contains(gene1));

        // Not reported
        assertTrue(breakendEvidence.findMatches(TestLinxFactory.breakendBuilder().gene("gene 1").reported(false).build()).isEmpty());

        // Wrong event
        assertTrue(breakendEvidence.findMatches(TestLinxFactory.breakendBuilder().gene("gene 2").reported(true).build()).isEmpty());
    }
}