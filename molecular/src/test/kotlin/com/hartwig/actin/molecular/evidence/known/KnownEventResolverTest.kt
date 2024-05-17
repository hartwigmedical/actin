package com.hartwig.actin.molecular.evidence.known

import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.evidence.TestMolecularFactory.minimalCopyNumber
import com.hartwig.actin.molecular.evidence.TestMolecularFactory.minimalDisruption
import com.hartwig.actin.molecular.evidence.TestMolecularFactory.minimalHomozygousDisruption
import com.hartwig.actin.molecular.evidence.matching.FUSION_CRITERIA
import com.hartwig.actin.molecular.evidence.matching.VARIANT_CRITERIA
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
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class KnownEventResolverTest {

    @Test
    fun `Should resolve known events for variants`() {
        val hotspot: KnownHotspot =
            TestServeKnownFactory.hotspotBuilder().gene("gene 1").chromosome("12").position(10).ref("A").alt("T").build()
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
        val known: KnownEvents =
            ImmutableKnownEvents.builder().addHotspots(hotspot).addCodons(codon).addExons(exon).addGenes(knownGene).build()
        val resolver = KnownEventResolver(known, known.genes())

        val hotspotMatch = VARIANT_CRITERIA.copy(
            gene = "gene 1",
            chromosome = "12",
            position = 10,
            ref = "A",
            alt = "T",
            codingEffect = CodingEffect.MISSENSE
        )

        assertEquals(hotspot, resolver.resolveForVariant(hotspotMatch))

        val codonMatch = hotspotMatch.copy(position = 9)
        assertEquals(codon, resolver.resolveForVariant(codonMatch))

        val exonMatch = hotspotMatch.copy(position = 6)
        assertEquals(exon, resolver.resolveForVariant(exonMatch))

        val geneMatch = hotspotMatch.copy(position = 1)
        assertNotNull(resolver.resolveForVariant(geneMatch))

        val wrongGene = hotspotMatch.copy(gene = "other")
        assertThat(resolver.resolveForVariant(wrongGene)).isNull()
    }

    @Test
    fun canResolveKnownEventsForGeneMutations() {
        val knownAmp: KnownCopyNumber = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.AMPLIFICATION).build()
        val knownDel: KnownCopyNumber = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.DELETION).build()
        val knownGene1: KnownGene = knownGeneWithName("gene 1")
        val knownGene2: KnownGene = knownGeneWithName("gene 2")
        val known: KnownEvents = ImmutableKnownEvents.builder().addCopyNumbers(knownAmp, knownDel).addGenes(knownGene1, knownGene2).build()
        val resolver = KnownEventResolver(known, known.genes())

        val ampGene1 = minimalCopyNumber().copy(gene = "gene 1", type = CopyNumberType.FULL_GAIN)
        assertEquals(knownAmp, resolver.resolveForCopyNumber(ampGene1))

        val ampGene2 = ampGene1.copy(gene = "gene 2")
        assertNotNull(resolver.resolveForCopyNumber(ampGene2))

        val ampGene3 = ampGene1.copy(gene = "gene 3")
        assertNull(resolver.resolveForCopyNumber(ampGene3))

        val homDisruptionGene1 = minimalHomozygousDisruption().copy(gene = "gene 1")
        assertEquals(knownDel, resolver.resolveForHomozygousDisruption(homDisruptionGene1))

        val homDisruptionGene2 = homDisruptionGene1.copy(gene = "gene 2")
        assertNotNull(resolver.resolveForHomozygousDisruption(homDisruptionGene2))

        val homDisruptionGene3 = homDisruptionGene1.copy(gene = "gene 3")
        assertNull(resolver.resolveForHomozygousDisruption(homDisruptionGene3))

        val disruptionGene1 = minimalDisruption().copy(gene = "gene 1")
        assertNotNull(resolver.resolveForBreakend(disruptionGene1))

        val disruptionGene2 = disruptionGene1.copy(gene = "gene 2")
        assertNotNull(resolver.resolveForBreakend(disruptionGene2))

        val disruptionGene3 = disruptionGene1.copy(gene = "gene 3")
        assertNull(resolver.resolveForBreakend(disruptionGene3))
    }

    @Test
    fun canResolveKnownEventsForFusions() {
        val fusion: KnownFusion = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").build()
        val known: KnownEvents = ImmutableKnownEvents.builder().addFusions(fusion).build()
        val resolver = KnownEventResolver(known, known.genes())

        val fusionMatch = FUSION_CRITERIA.copy(geneStart = "up", geneEnd = "down")
        assertEquals(fusion, resolver.resolveForFusion(fusionMatch))

        val fusionMismatch = FUSION_CRITERIA.copy(geneStart = "down", geneEnd = "up")
        assertNull(resolver.resolveForFusion(fusionMismatch))
    }

    companion object {
        private fun knownGeneWithName(name: String?): ImmutableKnownGene {
            return TestServeKnownFactory.geneBuilder().gene(name).build()
        }
    }
}