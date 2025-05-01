package com.hartwig.actin.molecular.evidence.known

import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.minimalCopyNumber
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.minimalDisruption
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.minimalHomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestVariantAlterationFactory
import com.hartwig.actin.datamodel.molecular.driver.VariantAlteration
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.serve.datamodel.molecular.ImmutableKnownEvents
import com.hartwig.serve.datamodel.molecular.MutationType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.molecular.gene.ImmutableKnownGene
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class KnownEventResolverTest {

    @Test
    fun `Should resolve known events for variants`() {
        val gene = "gene 1"
        val hotspot = TestServeKnownFactory.hotspotBuilder().gene(gene).chromosome("12").position(10).ref("A").alt("T").build()
        val codon = TestServeKnownFactory.codonBuilder()
            .gene(gene)
            .chromosome("12")
            .start(9)
            .end(11)
            .applicableMutationType(MutationType.ANY)
            .build()
        val exon = TestServeKnownFactory.exonBuilder()
            .gene(gene)
            .chromosome("12")
            .start(5)
            .end(15)
            .applicableMutationType(MutationType.ANY)
            .build()
        val knownGene = knownGeneWithName(gene)
        val knownEvents = ImmutableKnownEvents.builder().addHotspots(hotspot).addCodons(codon).addExons(exon).addGenes(knownGene).build()
        val filteredKnownEvents = ImmutableKnownEvents.builder().addCodons(codon).addExons(exon).addGenes(knownGene).build()
        val resolver = KnownEventResolver(knownEvents, filteredKnownEvents, knownEvents.genes())

        val hotspotMatch = VariantMatchCriteria(
            isReportable = true,
            gene = gene,
            codingEffect = CodingEffect.MISSENSE,
            type = VariantType.SNV,
            chromosome = "12",
            position = 10,
            ref = "A",
            alt = "T"
        )

        val alterationHotspot =
            TestVariantAlterationFactory.createProperVariantAlteration(gene, GeneRole.UNKNOWN, ProteinEffect.UNKNOWN, null, true)
        val alterationNoHotspot =
            TestVariantAlterationFactory.createProperVariantAlteration(gene, GeneRole.UNKNOWN, ProteinEffect.UNKNOWN, null, false)

        checkEquality(resolver.resolveForVariant(hotspotMatch), alterationHotspot)

        val codonMatch = hotspotMatch.copy(position = 9)
        checkEquality(resolver.resolveForVariant(codonMatch), alterationNoHotspot)

        val exonMatch = hotspotMatch.copy(position = 6)
        checkEquality(resolver.resolveForVariant(exonMatch), alterationNoHotspot)

        val geneMatch = hotspotMatch.copy(position = 1)
        assertThat(resolver.resolveForVariant(geneMatch)).isNotNull

        val wrongGene = hotspotMatch.copy(gene = "other")
        checkEquality(resolver.resolveForVariant(wrongGene), TestVariantAlterationFactory.createMinimalVariantAlteration("other"))
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

    private fun checkEquality(variantAlteration1: VariantAlteration, variantAlteration2: VariantAlteration) {
        assertThat(variantAlteration1.gene).isEqualTo(variantAlteration2.gene)
        assertThat(variantAlteration1.geneRole).isEqualTo(variantAlteration2.geneRole)
        assertThat(variantAlteration1.proteinEffect).isEqualTo(variantAlteration2.proteinEffect)
        assertThat(variantAlteration1.isAssociatedWithDrugResistance).isEqualTo(variantAlteration2.isAssociatedWithDrugResistance)
        assertThat(variantAlteration1.isHotspot).isEqualTo(variantAlteration2.isHotspot)
    }
}