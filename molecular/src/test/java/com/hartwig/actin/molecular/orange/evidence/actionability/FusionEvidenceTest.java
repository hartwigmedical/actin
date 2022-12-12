package com.hartwig.actin.molecular.orange.evidence.actionability;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusionType;
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.ImmutableActionableEvents;
import com.hartwig.serve.datamodel.fusion.ActionableFusion;
import com.hartwig.serve.datamodel.gene.ActionableGene;
import com.hartwig.serve.datamodel.gene.GeneEvent;

import org.junit.Test;

public class FusionEvidenceTest {

    @Test
    public void canDeterminePromiscuousFusionEvidence() {
        ActionableGene gene1 = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.FUSION).gene("gene 1").build();
        ActionableGene gene2 = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.ANY_MUTATION).gene("gene 2").build();
        ActionableGene gene3 = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.INACTIVATION).gene("gene 1").build();

        ActionableEvents actionable = ImmutableActionableEvents.builder().genes(Lists.newArrayList(gene1, gene2, gene3)).build();

        FusionEvidence fusionEvidence = FusionEvidence.create(actionable);

        LinxFusion reportedFusionGene1 =
                TestLinxFactory.fusionBuilder().geneStart("gene 1").type(LinxFusionType.PROMISCUOUS_5).reported(true).build();
        List<ActionableEvent> evidenceMatchGene1 = fusionEvidence.findMatches(reportedFusionGene1);

        assertEquals(1, evidenceMatchGene1.size());
        assertTrue(evidenceMatchGene1.contains(gene1));

        LinxFusion unreportedFusionGene1 =
                TestLinxFactory.fusionBuilder().geneStart("gene 1").type(LinxFusionType.PROMISCUOUS_5).reported(false).build();
        assertTrue(fusionEvidence.findMatches(unreportedFusionGene1).isEmpty());

        LinxFusion wrongTypeFusionGene1 =
                TestLinxFactory.fusionBuilder().geneStart("gene 1").type(LinxFusionType.PROMISCUOUS_3).reported(true).build();
        assertTrue(fusionEvidence.findMatches(wrongTypeFusionGene1).isEmpty());

        LinxFusion reportedFusionGene2 =
                TestLinxFactory.fusionBuilder().geneEnd("gene 2").type(LinxFusionType.PROMISCUOUS_3).reported(true).build();
        List<ActionableEvent> evidenceMatchGene2 = fusionEvidence.findMatches(reportedFusionGene2);

        assertEquals(1, evidenceMatchGene2.size());
        assertTrue(evidenceMatchGene2.contains(gene2));
    }

    @Test
    public void canDetermineEvidenceForKnownFusions() {
        ActionableFusion actionableFusion =
                TestServeActionabilityFactory.fusionBuilder().geneUp("up").geneDown("down").minExonUp(4).maxExonUp(6).build();

        ActionableEvents actionable = ImmutableActionableEvents.builder().addFusions(actionableFusion).build();

        FusionEvidence fusionEvidence = FusionEvidence.create(actionable);

        LinxFusion match = TestLinxFactory.fusionBuilder().geneStart("up").geneEnd("down").fusedExonUp(5).reported(true).build();
        List<ActionableEvent> evidences = fusionEvidence.findMatches(match);
        assertEquals(1, evidences.size());
        assertTrue(evidences.contains(actionableFusion));

        LinxFusion notReported = TestLinxFactory.fusionBuilder().geneStart("up").geneEnd("down").fusedExonUp(5).reported(false).build();
        assertTrue(fusionEvidence.findMatches(notReported).isEmpty());

        LinxFusion wrongExon = TestLinxFactory.fusionBuilder().geneStart("up").geneEnd("down").fusedExonUp(8).reported(true).build();
        assertTrue(fusionEvidence.findMatches(wrongExon).isEmpty());

        LinxFusion wrongGene = TestLinxFactory.fusionBuilder().geneStart("down").geneEnd("up").fusedExonUp(5).reported(true).build();
        assertTrue(fusionEvidence.findMatches(wrongGene).isEmpty());
    }
}