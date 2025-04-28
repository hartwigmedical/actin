package com.hartwig.actin.molecular.evidence.known

import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.minimalCopyNumber
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.minimalDisruption
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.minimalHomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.serve.datamodel.molecular.ImmutableKnownEvents
import com.hartwig.serve.datamodel.molecular.MutationType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.molecular.gene.ImmutableKnownGene
import com.hartwig.serve.datamodel.molecular.gene.KnownGene
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class KnownEventResolverTest {

    @Test
    fun `Should resolve known events for variants`() {
        val hotspot = TestServeKnownFactory.hotspotBuilder().gene("gene 1").chromosome("12").position(10).ref("A").alt("T").build()
        val codon = TestServeKnownFactory.codonBuilder()
            .gene("gene 1")
            .chromosome("12")
            .start(9)
            .end(11)
            .applicableMutationType(MutationType.ANY)
            .build()
        val exon = TestServeKnownFactory.exonBuilder()
            .gene("gene 1")
            .chromosome("12")
            .start(5)
            .end(15)
            .applicableMutationType(MutationType.ANY)
            .build()
        val knownGene = knownGeneWithName("gene 1")
        val knownEvents = ImmutableKnownEvents.builder().addHotspots(hotspot).addCodons(codon).addExons(exon).addGenes(knownGene).build()
        val resolver = KnownEventResolver(knownEvents, knownEvents, knownEvents.genes())

        val hotspotMatch = VariantMatchCriteria(
            isReportable = true,
            gene = "gene 1",
            codingEffect = CodingEffect.MISSENSE,
            type = VariantType.SNV,
            chromosome = "12",
            position = 10,
            ref = "A",
            alt = "T"
        )

        assertThat(resolver.resolveForVariant(hotspotMatch)).isEqualTo(listOf(hotspot))

        val codonMatch = hotspotMatch.copy(position = 9)
        assertThat(resolver.resolveForVariant(codonMatch)).isEqualTo(listOf(codon))

        val exonMatch = hotspotMatch.copy(position = 6)
        assertThat(resolver.resolveForVariant(exonMatch)).isEqualTo(listOf(exon))

        val geneMatch = hotspotMatch.copy(position = 1)
        assertThat(resolver.resolveForVariant(geneMatch)).isNotNull

        val wrongGene = hotspotMatch.copy(gene = "other")
        assertThat(resolver.resolveForVariant(wrongGene)).isEqualTo(emptyList<KnownGene>())
    }

    @Test
    fun `Should resolve known events for gene mutations`() {
        val knownAmp = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.AMPLIFICATION).build()
        val knownDel = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.DELETION).build()
        val knownGene1 = knownGeneWithName("gene 1")
        val knownGene2 = knownGeneWithName("gene 2")
        val knownEvents = ImmutableKnownEvents.builder().addCopyNumbers(knownAmp, knownDel).addGenes(knownGene1, knownGene2).build()
        val resolver = KnownEventResolver(knownEvents, knownEvents, knownEvents.genes())

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
        assertThat(resolver.resolveForDisruption(disruptionGene1)).isNotNull

        val disruptionGene2 = disruptionGene1.copy(gene = "gene 2")
        assertThat(resolver.resolveForDisruption(disruptionGene2)).isNotNull

        val disruptionGene3 = disruptionGene1.copy(gene = "gene 3")
        assertThat(resolver.resolveForDisruption(disruptionGene3)).isNull()
    }

    @Test
    fun `Should resolve known events for fusions`() {
        val fusion = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").build()
        val knownEvents = ImmutableKnownEvents.builder().addFusions(fusion).build()
        val resolver = KnownEventResolver(knownEvents, knownEvents, knownEvents.genes())

        val fusionMatch =
            FusionMatchCriteria(isReportable = true, geneStart = "up", geneEnd = "down", driverType = FusionDriverType.KNOWN_PAIR)
        assertThat(resolver.resolveForFusion(fusionMatch)).isEqualTo(fusion)

        val fusionMismatch = fusionMatch.copy(geneStart = "down", geneEnd = "up")
        assertThat(resolver.resolveForFusion(fusionMismatch)).isNull()
    }

    private fun knownGeneWithName(name: String?): ImmutableKnownGene {
        return TestServeKnownFactory.geneBuilder().gene(name!!).build()
    }
}