package com.hartwig.actin.molecular.evidence.known

import com.hartwig.actin.datamodel.molecular.CodingEffect
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.minimalCopyNumber
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.minimalDisruption
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.minimalHomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.evidence.matching.FUSION_CRITERIA
import com.hartwig.actin.molecular.evidence.matching.VARIANT_CRITERIA
import com.hartwig.serve.datamodel.molecular.ImmutableKnownEvents
import com.hartwig.serve.datamodel.molecular.KnownEvents
import com.hartwig.serve.datamodel.molecular.MutationType
import com.hartwig.serve.datamodel.molecular.fusion.KnownFusion
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.molecular.gene.ImmutableKnownGene
import com.hartwig.serve.datamodel.molecular.gene.KnownCopyNumber
import com.hartwig.serve.datamodel.molecular.gene.KnownGene
import com.hartwig.serve.datamodel.molecular.hotspot.KnownHotspot
import com.hartwig.serve.datamodel.molecular.range.KnownCodon
import com.hartwig.serve.datamodel.molecular.range.KnownExon
import org.assertj.core.api.Assertions.assertThat
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

        assertThat(resolver.resolveForVariant(hotspotMatch)).isEqualTo(hotspot)

        val codonMatch = hotspotMatch.copy(position = 9)
        assertThat(resolver.resolveForVariant(codonMatch)).isEqualTo(codon)

        val exonMatch = hotspotMatch.copy(position = 6)
        assertThat(resolver.resolveForVariant(exonMatch)).isEqualTo(exon)

        val geneMatch = hotspotMatch.copy(position = 1)
        assertThat(resolver.resolveForVariant(geneMatch)).isNotNull

        val wrongGene = hotspotMatch.copy(gene = "other")
        assertThat(resolver.resolveForVariant(wrongGene)).isNull()
    }

    @Test
    fun `Should resolve known events for gene mutations`() {
        val knownAmp: KnownCopyNumber = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.AMPLIFICATION).build()
        val knownDel: KnownCopyNumber = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.DELETION).build()
        val knownGene1: KnownGene = knownGeneWithName("gene 1")
        val knownGene2: KnownGene = knownGeneWithName("gene 2")
        val known: KnownEvents = ImmutableKnownEvents.builder().addCopyNumbers(knownAmp, knownDel).addGenes(knownGene1, knownGene2).build()
        val resolver = KnownEventResolver(known, known.genes())

        val ampGene1 = minimalCopyNumber().copy(
            gene = "gene 1",
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN)
        )
        assertThat(resolver.resolveForCopyNumber(ampGene1)).isEqualTo(knownAmp)

        val ampGene2 = ampGene1.copy(gene = "gene 2")
        assertThat(resolver.resolveForCopyNumber(ampGene2)).isNotNull

        val ampGene3 = ampGene1.copy(gene = "gene 3")
        assertThat(resolver.resolveForCopyNumber(ampGene3)).isNull()

        val homDisruptionGene1 = minimalHomozygousDisruption().copy(gene = "gene 1")
        assertThat(resolver.resolveForHomozygousDisruption(homDisruptionGene1)).isEqualTo(knownDel)

        val homDisruptionGene2 = homDisruptionGene1.copy(gene = "gene 2")
        assertThat(resolver.resolveForHomozygousDisruption(homDisruptionGene2)).isNotNull

        val homDisruptionGene3 = homDisruptionGene1.copy(gene = "gene 3")
        assertThat(resolver.resolveForHomozygousDisruption(homDisruptionGene3)).isNull()

        val disruptionGene1 = minimalDisruption().copy(gene = "gene 1")
        assertThat(resolver.resolveForBreakend(disruptionGene1)).isNotNull

        val disruptionGene2 = disruptionGene1.copy(gene = "gene 2")
        assertThat(resolver.resolveForBreakend(disruptionGene2)).isNotNull

        val disruptionGene3 = disruptionGene1.copy(gene = "gene 3")
        assertThat(resolver.resolveForBreakend(disruptionGene3)).isNull()
    }

    @Test
    fun `Should resolve known events for fusions`() {
        val fusion: KnownFusion = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").build()
        val known: KnownEvents = ImmutableKnownEvents.builder().addFusions(fusion).build()
        val resolver = KnownEventResolver(known, known.genes())

        val fusionMatch = FUSION_CRITERIA.copy(geneStart = "up", geneEnd = "down")
        assertThat(resolver.resolveForFusion(fusionMatch)).isEqualTo(fusion)

        val fusionMismatch = FUSION_CRITERIA.copy(geneStart = "down", geneEnd = "up")
        assertThat(resolver.resolveForFusion(fusionMismatch)).isNull()
    }

    private fun knownGeneWithName(name: String?): ImmutableKnownGene {
        return TestServeKnownFactory.geneBuilder().gene(name!!).build()
    }
}