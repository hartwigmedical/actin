package com.hartwig.actin.molecular.orange.evidence.known;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxBreakend;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCodingEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleGainLoss;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleGainLossInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.actin.molecular.serve.KnownGene;
import com.hartwig.actin.molecular.serve.TestKnownGeneFactory;
import com.hartwig.serve.datamodel.ImmutableKnownEvents;
import com.hartwig.serve.datamodel.KnownEvents;
import com.hartwig.serve.datamodel.MutationType;
import com.hartwig.serve.datamodel.fusion.KnownFusion;
import com.hartwig.serve.datamodel.gene.GeneEvent;
import com.hartwig.serve.datamodel.gene.KnownCopyNumber;
import com.hartwig.serve.datamodel.hotspot.KnownHotspot;
import com.hartwig.serve.datamodel.range.KnownCodon;
import com.hartwig.serve.datamodel.range.KnownExon;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class KnownEventResolverTest {

    @Test
    public void canResolveKnownEventsForVariants() {
        KnownHotspot hotspot =
                TestServeKnownFactory.hotspotBuilder().gene("gene 1").chromosome("12").position(10).ref("A").alt("T").build();
        KnownCodon codon = TestServeKnownFactory.codonBuilder()
                .gene("gene 1")
                .chromosome("12")
                .start(9)
                .end(11)
                .applicableMutationType(MutationType.ANY)
                .build();
        KnownExon exon = TestServeKnownFactory.exonBuilder()
                .gene("gene 1")
                .chromosome("12")
                .start(5)
                .end(15)
                .applicableMutationType(MutationType.ANY)
                .build();

        KnownEvents known = ImmutableKnownEvents.builder().addHotspots(hotspot).addCodons(codon).addExons(exon).build();
        KnownGene knownGene = TestKnownGeneFactory.builder().gene("gene 1").build();
        KnownEventResolver resolver = new KnownEventResolver(known, Lists.newArrayList(knownGene));

        PurpleVariant hotspotMatch = TestPurpleFactory.variantBuilder()
                .gene("gene 1")
                .chromosome("12")
                .position(10)
                .ref("A")
                .alt("T")
                .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().codingEffect(PurpleCodingEffect.MISSENSE).build())
                .build();

        assertEquals(hotspot, resolver.resolveForVariant(hotspotMatch));

        PurpleVariant codonMatch = TestPurpleFactory.variantBuilder().from(hotspotMatch).position(9).build();
        assertEquals(codon, resolver.resolveForVariant(codonMatch));

        PurpleVariant exonMatch = TestPurpleFactory.variantBuilder().from(hotspotMatch).position(6).build();
        assertEquals(exon, resolver.resolveForVariant(exonMatch));

        PurpleVariant geneMatch = TestPurpleFactory.variantBuilder().from(hotspotMatch).position(1).build();
        assertNotNull(resolver.resolveForVariant(geneMatch));

        PurpleVariant wrongGene = TestPurpleFactory.variantBuilder().from(hotspotMatch).gene("other").build();
        assertNull(resolver.resolveForVariant(wrongGene));
    }

    @Test
    public void canResolveKnownEventsForGeneMutations() {
        KnownCopyNumber knownAmp = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.AMPLIFICATION).build();
        KnownCopyNumber knownDel = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.DELETION).build();

        KnownEvents known = ImmutableKnownEvents.builder().addCopyNumbers(knownAmp, knownDel).build();

        KnownGene knownGene1 = TestKnownGeneFactory.builder().gene("gene 1").build();
        KnownGene knownGene2 = TestKnownGeneFactory.builder().gene("gene 2").build();
        KnownEventResolver resolver = new KnownEventResolver(known, Lists.newArrayList(knownGene1, knownGene2));

        PurpleGainLoss ampGene1 = amp("gene 1");
        assertEquals(knownAmp, resolver.resolveForCopyNumber(ampGene1));

        PurpleGainLoss ampGene2 = amp("gene 2");
        assertNotNull(resolver.resolveForCopyNumber(ampGene2));

        PurpleGainLoss ampGene3 = amp("gene 3");
        assertNull(resolver.resolveForCopyNumber(ampGene3));

        LinxHomozygousDisruption homDisruptionGene1 = TestLinxFactory.homozygousDisruptionBuilder().gene("gene 1").build();
        assertEquals(knownDel, resolver.resolveForHomozygousDisruption(homDisruptionGene1));

        LinxHomozygousDisruption homDisruptionGene2 = TestLinxFactory.homozygousDisruptionBuilder().gene("gene 2").build();
        assertNotNull(resolver.resolveForHomozygousDisruption(homDisruptionGene2));

        LinxHomozygousDisruption homDisruptionGene3 = TestLinxFactory.homozygousDisruptionBuilder().gene("gene 3").build();
        assertNull(resolver.resolveForHomozygousDisruption(homDisruptionGene3));

        LinxBreakend breakendGene1 = TestLinxFactory.breakendBuilder().gene("gene 1").build();
        assertNotNull(resolver.resolveForBreakend(breakendGene1));

        LinxBreakend breakendGene2 = TestLinxFactory.breakendBuilder().gene("gene 2").build();
        assertNotNull(resolver.resolveForBreakend(breakendGene2));

        LinxBreakend breakendGene3 = TestLinxFactory.breakendBuilder().gene("gene 3").build();
        assertNull(resolver.resolveForBreakend(breakendGene3));
    }

    @Test
    public void canResolveKnownEventsForFusions() {
        KnownFusion fusion = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").build();

        KnownEvents known = ImmutableKnownEvents.builder().addFusions(fusion).build();

        KnownGene gene1 = TestKnownGeneFactory.builder().gene("gene 1").build();
        KnownEventResolver resolver = new KnownEventResolver(known, Lists.newArrayList(gene1));

        LinxFusion fusionMatch = TestLinxFactory.fusionBuilder().geneStart("up").geneEnd("down").build();
        assertEquals(fusion, resolver.resolveForFusion(fusionMatch));

        LinxFusion fusionMismatch = TestLinxFactory.fusionBuilder().geneStart("down").geneEnd("up").build();
        assertNull(resolver.resolveForFusion(fusionMismatch));
    }

    @NotNull
    private static PurpleGainLoss amp(@NotNull String gene) {
        return TestPurpleFactory.gainLossBuilder().gene(gene).interpretation(PurpleGainLossInterpretation.FULL_GAIN).build();
    }
}