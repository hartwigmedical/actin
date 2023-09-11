package com.hartwig.actin.molecular.orange.evidence.actionability;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCodingEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleTranscriptImpact;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.ImmutableActionableEvents;
import com.hartwig.serve.datamodel.MutationType;
import com.hartwig.serve.datamodel.gene.ActionableGene;
import com.hartwig.serve.datamodel.gene.GeneEvent;
import com.hartwig.serve.datamodel.hotspot.ActionableHotspot;
import com.hartwig.serve.datamodel.range.ImmutableActionableRange;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class VariantEvidenceTest {

    public static final ImmutableActionableRange ACTIONABLE_RANGE = TestServeActionabilityFactory.rangeBuilder()
            .gene("gene 1")
            .chromosome("X")
            .start(4)
            .end(8)
            .applicableMutationType(MutationType.ANY)
            .build();

    @Test
    public void shouldDetermineEvidenceForHotspots() {
        ActionableHotspot hotspot1 =
                TestServeActionabilityFactory.hotspotBuilder().gene("gene 1").chromosome("X").position(2).ref("A").alt("G").build();
        ActionableHotspot hotspot2 =
                TestServeActionabilityFactory.hotspotBuilder().gene("gene 2").chromosome("X").position(2).ref("A").alt("G").build();
        ActionableHotspot hotspot3 =
                TestServeActionabilityFactory.hotspotBuilder().gene("gene 1").chromosome("X").position(2).ref("A").alt("C").build();

        ActionableEvents actionable = ImmutableActionableEvents.builder().addAllHotspots(List.of(hotspot1, hotspot2, hotspot3)).build();

        VariantEvidence variantEvidence = VariantEvidence.create(actionable);

        PurpleVariant variantGene1 =
                TestPurpleFactory.variantBuilder().gene("gene 1").chromosome("X").position(2).ref("A").alt("G").reported(true).build();
        List<ActionableEvent> matchesVariant1 = variantEvidence.findMatches(variantGene1);
        assertEquals(1, matchesVariant1.size());
        assertTrue(matchesVariant1.contains(hotspot1));

        PurpleVariant variantGene2 =
                TestPurpleFactory.variantBuilder().gene("gene 2").chromosome("X").position(2).ref("A").alt("G").reported(true).build();
        List<ActionableEvent> matchesVariant2 = variantEvidence.findMatches(variantGene2);
        assertEquals(1, matchesVariant2.size());
        assertTrue(matchesVariant2.contains(hotspot2));

        PurpleVariant otherVariantGene1 =
                TestPurpleFactory.variantBuilder().gene("gene 1").chromosome("X").position(2).ref("A").alt("T").reported(true).build();
        assertTrue(variantEvidence.findMatches(otherVariantGene1).isEmpty());
    }

    @Test
    public void shouldDetermineEvidenceForCodons() {
        assertEvidenceDeterminedForRange(ImmutableActionableEvents.builder().addCodons(ACTIONABLE_RANGE).build());
    }

    @Test
    public void shouldDetermineEvidenceForExons() {
        assertEvidenceDeterminedForRange(ImmutableActionableEvents.builder().addExons(ACTIONABLE_RANGE).build());
    }

    private void assertEvidenceDeterminedForRange(@NotNull ActionableEvents actionable) {
        VariantEvidence variantEvidence = VariantEvidence.create(actionable);

        PurpleVariant variantGene1 = TestPurpleFactory.variantBuilder()
                .gene("gene 1")
                .chromosome("X")
                .position(6)
                .reported(true)
                .canonicalImpact(create(PurpleCodingEffect.MISSENSE))
                .build();
        List<ActionableEvent> matchesVariant1 = variantEvidence.findMatches(variantGene1);
        assertEquals(1, matchesVariant1.size());
        assertTrue(matchesVariant1.contains(ACTIONABLE_RANGE));

        PurpleVariant otherVariantGene1 = TestPurpleFactory.variantBuilder()
                .gene("gene 1")
                .chromosome("X")
                .position(2)
                .canonicalImpact(create(PurpleCodingEffect.MISSENSE))
                .reported(true)
                .build();
        assertTrue(variantEvidence.findMatches(otherVariantGene1).isEmpty());
    }

    @Test
    public void shouldDetermineEvidenceForGenes() {
        ActionableGene gene1 = TestServeActionabilityFactory.geneBuilder().gene("gene 1").event(GeneEvent.ANY_MUTATION).build();
        ActionableGene gene2 = TestServeActionabilityFactory.geneBuilder().gene("gene 2").event(GeneEvent.ACTIVATION).build();
        ActionableGene gene3 = TestServeActionabilityFactory.geneBuilder().gene("gene 2").event(GeneEvent.AMPLIFICATION).build();

        ActionableEvents actionable = ImmutableActionableEvents.builder().addGenes(gene1, gene2, gene3).build();

        VariantEvidence variantEvidence = VariantEvidence.create(actionable);

        PurpleVariant variantGene1 = TestPurpleFactory.variantBuilder()
                .gene("gene 1")
                .canonicalImpact(create(PurpleCodingEffect.MISSENSE))
                .reported(true)
                .build();

        List<ActionableEvent> matchesVariant1 = variantEvidence.findMatches(variantGene1);
        assertEquals(1, matchesVariant1.size());
        assertTrue(matchesVariant1.contains(gene1));

        PurpleVariant variantGene2 = TestPurpleFactory.variantBuilder()
                .gene("gene 2")
                .canonicalImpact(create(PurpleCodingEffect.MISSENSE))
                .reported(true)
                .build();

        List<ActionableEvent> matchesVariant2 = variantEvidence.findMatches(variantGene2);
        assertEquals(1, matchesVariant2.size());
        assertTrue(matchesVariant2.contains(gene2));
    }

    @NotNull
    private static PurpleTranscriptImpact create(@NotNull PurpleCodingEffect codingEffect) {
        return TestPurpleFactory.transcriptImpactBuilder().codingEffect(codingEffect).build();
    }
}