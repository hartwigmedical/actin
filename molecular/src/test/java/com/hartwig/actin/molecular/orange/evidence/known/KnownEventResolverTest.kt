package com.hartwig.actin.molecular.orange.evidence.known

import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.hmftools.datamodel.linx.LinxBreakend
import com.hartwig.hmftools.datamodel.linx.LinxFusion
import com.hartwig.hmftools.datamodel.linx.LinxHomozygousDisruption
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.serve.datamodel.ImmutableKnownEvents
import com.hartwig.serve.datamodel.KnownEvents
import com.hartwig.serve.datamodel.MutationType
import com.hartwig.serve.datamodel.fusion.KnownFusion
import com.hartwig.serve.datamodel.gene.GeneEvent
import com.hartwig.serve.datamodel.gene.ImmutableKnownGene
import com.hartwig.serve.datamodel.gene.KnownCopyNumber
import com.hartwig.serve.datamodel.gene.KnownGene
import com.hartwig.serve.datamodel.hotspot.KnownHotspot
import com.hartwig.serve.datamodel.range.KnownCodon
import com.hartwig.serve.datamodel.range.KnownExon
import org.junit.Assert
import org.junit.Test

class KnownEventResolverTest {
    @Test
    fun canResolveKnownEventsForVariants() {
        val hotspot: KnownHotspot = TestServeKnownFactory.hotspotBuilder().gene("gene 1").chromosome("12").position(10).ref("A").alt("T").build()
        val codon: KnownCodon = TestServeKnownFactory.codonBuilder()
            .gene("gene 1")
            .chromosome("12")
            .start(9)
            .end(11)
            .applicableMutationType(MutationType.ANY)
            .build()
        val exon: KnownExon = TestServeKnownFactory.exonBuilder()
            .gene("gene 1")
            .chromosome("12")
            .start(5)
            .end(15)
            .applicableMutationType(MutationType.ANY)
            .build()
        val knownGene: KnownGene = knownGeneWithName("gene 1")
        val known: KnownEvents = ImmutableKnownEvents.builder().addHotspots(hotspot).addCodons(codon).addExons(exon).addGenes(knownGene).build()
        val resolver = KnownEventResolver(known, known.genes())
        val hotspotMatch: PurpleVariant = TestPurpleFactory.variantBuilder()
            .gene("gene 1")
            .chromosome("12")
            .position(10)
            .ref("A")
            .alt("T")
            .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().codingEffect(PurpleCodingEffect.MISSENSE).build())
            .build()
        Assert.assertEquals(hotspot, resolver.resolveForVariant(hotspotMatch))
        val codonMatch: PurpleVariant = TestPurpleFactory.variantBuilder().from(hotspotMatch).position(9).build()
        Assert.assertEquals(codon, resolver.resolveForVariant(codonMatch))
        val exonMatch: PurpleVariant = TestPurpleFactory.variantBuilder().from(hotspotMatch).position(6).build()
        Assert.assertEquals(exon, resolver.resolveForVariant(exonMatch))
        val geneMatch: PurpleVariant = TestPurpleFactory.variantBuilder().from(hotspotMatch).position(1).build()
        Assert.assertNotNull(resolver.resolveForVariant(geneMatch))
        val wrongGene: PurpleVariant = TestPurpleFactory.variantBuilder().from(hotspotMatch).gene("other").build()
        Assert.assertNull(resolver.resolveForVariant(wrongGene))
    }

    @Test
    fun canResolveKnownEventsForGeneMutations() {
        val knownAmp: KnownCopyNumber = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.AMPLIFICATION).build()
        val knownDel: KnownCopyNumber = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.DELETION).build()
        val knownGene1: KnownGene = knownGeneWithName("gene 1")
        val knownGene2: KnownGene = knownGeneWithName("gene 2")
        val known: KnownEvents = ImmutableKnownEvents.builder().addCopyNumbers(knownAmp, knownDel).addGenes(knownGene1, knownGene2).build()
        val resolver = KnownEventResolver(known, known.genes())
        val ampGene1 = amp("gene 1")
        Assert.assertEquals(knownAmp, resolver.resolveForCopyNumber(ampGene1))
        val ampGene2 = amp("gene 2")
        Assert.assertNotNull(resolver.resolveForCopyNumber(ampGene2))
        val ampGene3 = amp("gene 3")
        Assert.assertNull(resolver.resolveForCopyNumber(ampGene3))
        val homDisruptionGene1: LinxHomozygousDisruption = TestLinxFactory.homozygousDisruptionBuilder().gene("gene 1").build()
        Assert.assertEquals(knownDel, resolver.resolveForHomozygousDisruption(homDisruptionGene1))
        val homDisruptionGene2: LinxHomozygousDisruption = TestLinxFactory.homozygousDisruptionBuilder().gene("gene 2").build()
        Assert.assertNotNull(resolver.resolveForHomozygousDisruption(homDisruptionGene2))
        val homDisruptionGene3: LinxHomozygousDisruption = TestLinxFactory.homozygousDisruptionBuilder().gene("gene 3").build()
        Assert.assertNull(resolver.resolveForHomozygousDisruption(homDisruptionGene3))
        val breakendGene1: LinxBreakend = TestLinxFactory.breakendBuilder().gene("gene 1").build()
        Assert.assertNotNull(resolver.resolveForBreakend(breakendGene1))
        val breakendGene2: LinxBreakend = TestLinxFactory.breakendBuilder().gene("gene 2").build()
        Assert.assertNotNull(resolver.resolveForBreakend(breakendGene2))
        val breakendGene3: LinxBreakend = TestLinxFactory.breakendBuilder().gene("gene 3").build()
        Assert.assertNull(resolver.resolveForBreakend(breakendGene3))
    }

    @Test
    fun canResolveKnownEventsForFusions() {
        val fusion: KnownFusion = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").build()
        val known: KnownEvents = ImmutableKnownEvents.builder().addFusions(fusion).build()
        val resolver = KnownEventResolver(known, known.genes())
        val fusionMatch: LinxFusion = TestLinxFactory.fusionBuilder().geneStart("up").geneEnd("down").build()
        Assert.assertEquals(fusion, resolver.resolveForFusion(fusionMatch))
        val fusionMismatch: LinxFusion = TestLinxFactory.fusionBuilder().geneStart("down").geneEnd("up").build()
        Assert.assertNull(resolver.resolveForFusion(fusionMismatch))
    }

    companion object {
        private fun amp(gene: String): PurpleGainLoss {
            return TestPurpleFactory.gainLossBuilder().gene(gene).interpretation(CopyNumberInterpretation.FULL_GAIN).build()
        }

        private fun knownGeneWithName(name: String?): ImmutableKnownGene {
            return TestServeKnownFactory.geneBuilder().gene(name).build()
        }
    }
}