package com.hartwig.actin.molecular.orange.evidence.actionability;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleGainLoss;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleGainLossInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.ImmutableActionableEvents;
import com.hartwig.serve.datamodel.gene.ActionableGene;
import com.hartwig.serve.datamodel.gene.GeneEvent;

import org.junit.Test;

public class CopyNumberEvidenceTest {

    @Test
    public void canDetermineCopyNumberEvidence() {
        ActionableGene gene1 = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.AMPLIFICATION).gene("gene 1").build();
        ActionableGene gene2 = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.DELETION).gene("gene 2").build();
        ActionableGene gene3 = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.INACTIVATION).gene("gene 1").build();

        ActionableEvents actionable = ImmutableActionableEvents.builder().genes(Lists.newArrayList(gene1, gene2, gene3)).build();

        CopyNumberEvidence copyNumberEvidence = CopyNumberEvidence.create(actionable);

        PurpleGainLoss ampGene1 =
                TestPurpleFactory.gainLossBuilder().gene("gene 1").interpretation(PurpleGainLossInterpretation.FULL_GAIN).build();
        List<ActionableEvent> ampMatches = copyNumberEvidence.findMatches(ampGene1);

        assertEquals(1, ampMatches.size());
        assertTrue(ampMatches.contains(gene1));

        PurpleGainLoss lossGene2 =
                TestPurpleFactory.gainLossBuilder().gene("gene 2").interpretation(PurpleGainLossInterpretation.FULL_LOSS).build();
        List<ActionableEvent> delMatches = copyNumberEvidence.findMatches(lossGene2);

        assertEquals(1, delMatches.size());
        assertTrue(delMatches.contains(gene2));

        PurpleGainLoss lossGene1 =
                TestPurpleFactory.gainLossBuilder().gene("gene 1").interpretation(PurpleGainLossInterpretation.FULL_LOSS).build();
        assertTrue(copyNumberEvidence.findMatches(lossGene1).isEmpty());
    }

}