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

public class HomozygousDisruptionEvidenceTest {

    @Test
    public void canDetermineHomozygousDisruptionEvidence() {
        ActionableGene gene1 = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.DELETION).gene("gene 1").build();
        ActionableGene gene2 = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.INACTIVATION).gene("gene 2").build();
        ActionableGene gene3 = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.AMPLIFICATION).gene("gene 3").build();

        ActionableEvents actionable = ImmutableActionableEvents.builder().genes(Lists.newArrayList(gene1, gene2, gene3)).build();

        HomozygousDisruptionEvidence homozygousDisruptionEvidence = HomozygousDisruptionEvidence.create(actionable);

        List<ActionableEvent> matchGene1 =
                homozygousDisruptionEvidence.findMatches(TestLinxFactory.homozygousDisruptionBuilder().gene("gene 1").build());
        assertEquals(1, matchGene1.size());
        assertTrue(matchGene1.contains(gene1));

        List<ActionableEvent> matchGene2 =
                homozygousDisruptionEvidence.findMatches(TestLinxFactory.homozygousDisruptionBuilder().gene("gene 2").build());
        assertEquals(1, matchGene2.size());
        assertTrue(matchGene2.contains(gene2));

        assertTrue(homozygousDisruptionEvidence.findMatches(TestLinxFactory.homozygousDisruptionBuilder().gene("gene 3").build())
                .isEmpty());
    }
}