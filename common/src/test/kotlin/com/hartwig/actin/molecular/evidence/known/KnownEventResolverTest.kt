package com.hartwig.actin.molecular.evidence.known

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.createMinimalCopyNumber
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.createMinimalDisruption
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.createMinimalHomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.actin.datamodel.molecular.driver.GeneAlteration
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestGeneAlterationFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantAlterationFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.VariantAlteration
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect as ServeProteinEffect
import com.hartwig.serve.datamodel.molecular.ImmutableKnownEvents
import com.hartwig.serve.datamodel.molecular.MutationType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.molecular.gene.ImmutableKnownGene
import com.hartwig.serve.datamodel.molecular.hotspot.ImmutableKnownHotspot
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val GENE = "gene 1"
private val CANCER_ASSOCIATED_VARIANT_MATCH = TestMolecularFactory.createMinimalVariant().copy(
    isReportable = true,
    gene = GENE,
    canonicalImpact = TestMolecularFactory.createMinimalTranscriptImpact().copy(codingEffect = CodingEffect.MISSENSE),
    type = VariantType.SNV,
    chromosome = "12",
    position = 10,
    ref = "A",
    alt = "T"
)

class KnownEventResolverTest {

    private val hotspotDoCM = createHotspot(Knowledgebase.DOCM)
    private val knownGene = knownGeneWithName(GENE)

    @Test
    fun `Should resolve variant when variant is cancer-associated variant in CKB and in DoCM`() {
        val hotspotCkb = createHotspot(Knowledgebase.CKB, proteinEffect = ServeProteinEffect.GAIN_OF_FUNCTION)
        val primaryKnownEvents = ImmutableKnownEvents.builder().addHotspots(hotspotCkb).addGenes(knownGene).build()
        val secondaryKnownEvents = ImmutableKnownEvents.builder().addHotspots(hotspotDoCM).build()
        val resolver = KnownEventResolver(primaryKnownEvents, secondaryKnownEvents, primaryKnownEvents.genes())

        val cancerAssociatedVariantAlteration = TestVariantAlterationFactory.createVariantAlteration(
            GENE,
            proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
            isCancerAssociatedVariant = true
        )
        assertEquality(resolver.resolveForVariant(CANCER_ASSOCIATED_VARIANT_MATCH), cancerAssociatedVariantAlteration)
    }

    @Test
    fun `Should resolve variant when variant is cancer-associated variant in DoCM and not present in CKB`() {
        val primaryKnownEvents = ImmutableKnownEvents.builder().addGenes(knownGene).build()
        val secondaryKnownEvents = ImmutableKnownEvents.builder().addHotspots(hotspotDoCM).build()
        val resolver = KnownEventResolver(primaryKnownEvents, secondaryKnownEvents, primaryKnownEvents.genes())

        val cancerAssociatedVariantAlteration = TestVariantAlterationFactory.createVariantAlteration(GENE, isCancerAssociatedVariant = true)
        assertEquality(resolver.resolveForVariant(CANCER_ASSOCIATED_VARIANT_MATCH), cancerAssociatedVariantAlteration)
    }

    @Test
    fun `Should resolve variant when variant is not a cancer-associated variant in any source`() {
        val hotspotCkb = createHotspot(Knowledgebase.CKB, ServeProteinEffect.NO_EFFECT)
        val primaryKnownEvents = ImmutableKnownEvents.builder().addHotspots(hotspotCkb).addGenes(knownGene).build()
        val secondaryKnownEvents = ImmutableKnownEvents.builder().build()
        val resolver = KnownEventResolver(primaryKnownEvents, secondaryKnownEvents, primaryKnownEvents.genes())

        val cancerAssociatedVariantAlteration = TestVariantAlterationFactory.createVariantAlteration(
            GENE,
            proteinEffect = ProteinEffect.NO_EFFECT,
            isCancerAssociatedVariant = false
        )
        assertEquality(resolver.resolveForVariant(CANCER_ASSOCIATED_VARIANT_MATCH), cancerAssociatedVariantAlteration)
    }

    @Test
    fun `Should resolve variant when variant is not in CKB and not a cancer-associated variant in any other source`() {
        val primaryKnownEvents = ImmutableKnownEvents.builder().addGenes(knownGene).build()
        val secondaryKnownEvents = ImmutableKnownEvents.builder().build()
        val resolver = KnownEventResolver(primaryKnownEvents, secondaryKnownEvents, primaryKnownEvents.genes())

        val cancerAssociatedVariantAlteration =
            TestVariantAlterationFactory.createVariantAlteration(GENE, isCancerAssociatedVariant = false)
        assertEquality(resolver.resolveForVariant(CANCER_ASSOCIATED_VARIANT_MATCH), cancerAssociatedVariantAlteration)
    }

    @Test
    fun `Should resolve variant when variant is cancer-associated variant in DoCM`() {
        val hotspotCkb = createHotspot(Knowledgebase.CKB, ServeProteinEffect.NO_EFFECT)
        val primaryKnownEvents = ImmutableKnownEvents.builder().addHotspots(hotspotCkb).addGenes(knownGene).build()
        val secondaryKnownEvents = ImmutableKnownEvents.builder().addHotspots(hotspotDoCM).build()
        val resolver = KnownEventResolver(primaryKnownEvents, secondaryKnownEvents, primaryKnownEvents.genes())

        val cancerAssociatedVariantAlteration = TestVariantAlterationFactory.createVariantAlteration(
            GENE,
            proteinEffect = ProteinEffect.NO_EFFECT,
            isCancerAssociatedVariant = true
        )
        assertEquality(resolver.resolveForVariant(CANCER_ASSOCIATED_VARIANT_MATCH), cancerAssociatedVariantAlteration)
    }

    @Test
    fun `Should resolve variants when exon or codon match`() {
        val hotspot = TestServeKnownFactory.hotspotBuilder().gene(GENE).chromosome("12").position(10).ref("A").alt("T").build()
        val codon = TestServeKnownFactory.codonBuilder()
            .gene(GENE)
            .chromosome("12")
            .start(9)
            .end(11)
            .applicableMutationType(MutationType.ANY)
            .build()
        val exon = TestServeKnownFactory.exonBuilder()
            .gene(GENE)
            .chromosome("12")
            .start(5)
            .end(15)
            .applicableMutationType(MutationType.ANY)
            .build()
        val primaryKnownEvents = ImmutableKnownEvents.builder().addCodons(codon).addExons(exon).addGenes(knownGene).build()
        val secondaryKnownEvents = ImmutableKnownEvents.builder().addHotspots(hotspot).build()
        val resolver = KnownEventResolver(primaryKnownEvents, secondaryKnownEvents, primaryKnownEvents.genes())

        val cancerAssociatedVariantAlteration = TestVariantAlterationFactory.createVariantAlteration(GENE, isCancerAssociatedVariant = true)
        val noCancerAssociatedVariantAlteration =
            TestVariantAlterationFactory.createVariantAlteration(GENE, isCancerAssociatedVariant = false)

        assertEquality(resolver.resolveForVariant(CANCER_ASSOCIATED_VARIANT_MATCH), cancerAssociatedVariantAlteration)

        val codonMatch = CANCER_ASSOCIATED_VARIANT_MATCH.copy(position = 9)
        assertEquality(resolver.resolveForVariant(codonMatch), noCancerAssociatedVariantAlteration)

        val exonMatch = CANCER_ASSOCIATED_VARIANT_MATCH.copy(position = 6)
        assertEquality(resolver.resolveForVariant(exonMatch), noCancerAssociatedVariantAlteration)

        val geneMatch = CANCER_ASSOCIATED_VARIANT_MATCH.copy(position = 1)
        assertEquality(resolver.resolveForVariant(geneMatch), noCancerAssociatedVariantAlteration)

        val wrongGene = CANCER_ASSOCIATED_VARIANT_MATCH.copy(gene = "other")
        assertEquality(resolver.resolveForVariant(wrongGene), TestVariantAlterationFactory.createVariantAlteration("other"))
    }

    @Test
    fun `Should resolve known events for gene mutations`() {
        val knownAmp = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.AMPLIFICATION).build()
        val knownDel = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.DELETION).build()
        val knownGene1 = knownGeneWithName("gene 1")
        val knownGene2 = knownGeneWithName("gene 2")
        val primaryKnownEvents = ImmutableKnownEvents.builder().addCopyNumbers(knownAmp, knownDel).addGenes(knownGene1, knownGene2).build()
        val secondaryKnownEvents = ImmutableKnownEvents.builder().build()
        val resolver = KnownEventResolver(primaryKnownEvents, secondaryKnownEvents, primaryKnownEvents.genes())

        val ampGene1 = createMinimalCopyNumber().copy(
            gene = "gene 1",
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN)
        )

        assertEquality(resolver.resolveForCopyNumber(ampGene1), TestGeneAlterationFactory.createGeneAlteration("gene 1"))

        val ampGene2 = ampGene1.copy(gene = "gene 2")
        assertEquality(resolver.resolveForCopyNumber(ampGene2), TestGeneAlterationFactory.createGeneAlteration("gene 2"))

        val homDisruptionGene1 = createMinimalHomozygousDisruption().copy(gene = "gene 1")
        assertEquality(
            resolver.resolveForHomozygousDisruption(homDisruptionGene1),
            TestGeneAlterationFactory.createGeneAlteration("gene 1")
        )

        val homDisruptionGene2 = homDisruptionGene1.copy(gene = "gene 2")
        assertEquality(
            resolver.resolveForHomozygousDisruption(homDisruptionGene2),
            TestGeneAlterationFactory.createGeneAlteration("gene 2")
        )

        val disruptionGene1 = createMinimalDisruption().copy(gene = "gene 1")
        assertEquality(resolver.resolveForDisruption(disruptionGene1), TestGeneAlterationFactory.createGeneAlteration("gene 1"))

        val disruptionGene3 = disruptionGene1.copy(gene = "gene 3")
        assertEquality(resolver.resolveForDisruption(disruptionGene3), TestGeneAlterationFactory.createGeneAlteration("gene 3"))
    }

    @Test
    fun `Should resolve known events for fusions`() {
        val fusion = TestServeKnownFactory.fusionBuilder().geneUp("up").geneDown("down").build()
        val primaryKnownEvents = ImmutableKnownEvents.builder().addFusions(fusion).build()
        val secondaryKnownEvents = ImmutableKnownEvents.builder().build()
        val resolver = KnownEventResolver(primaryKnownEvents, secondaryKnownEvents, primaryKnownEvents.genes())

        val fusionMatch = TestMolecularFactory.createMinimalFusion()
            .copy(isReportable = true, geneStart = "up", geneEnd = "down", driverType = FusionDriverType.KNOWN_PAIR)
        assertThat(resolver.resolveForFusion(fusionMatch)).isEqualTo(fusion)

        val fusionMismatch = fusionMatch.copy(geneStart = "down", geneEnd = "up")
        val otherFusion = TestServeKnownFactory.fusionBuilder().geneUp("down").geneDown("up").build()
        assertThat(resolver.resolveForFusion(fusionMismatch)).isEqualTo(otherFusion)
    }

    @Test
    fun `Should annotate protein effect for frameshift in TSG`() {
        val variant = TestVariantFactory.createMinimal().copy(
            gene = GENE,
            canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(codingEffect = CodingEffect.NONSENSE_OR_FRAMESHIFT)
        )
        val knownGene =
            TestServeKnownFactory.geneBuilder().gene(GENE).geneRole(com.hartwig.serve.datamodel.molecular.common.GeneRole.TSG).build()
        val knownEvents = ImmutableKnownEvents.builder().addGenes(knownGene).build()
        val resolver = KnownEventResolver(knownEvents, knownEvents, knownEvents.genes())
        val output = resolver.resolveForVariant(variant)
        assertThat(output.proteinEffect).isEqualTo(ProteinEffect.LOSS_OF_FUNCTION_PREDICTED)
    }

    private fun createHotspot(
        source: Knowledgebase = Knowledgebase.CKB,
        proteinEffect: ServeProteinEffect = ServeProteinEffect.UNKNOWN
    ): ImmutableKnownHotspot {
        return TestServeKnownFactory.hotspotBuilder().gene(GENE).chromosome("12").position(10).ref("A").alt("T")
            .proteinEffect(proteinEffect).sources(setOf(source)).build()
    }

    private fun knownGeneWithName(name: String?): ImmutableKnownGene {
        return TestServeKnownFactory.geneBuilder().gene(name!!).build()
    }

    private fun <T> assertEquality(alteration1: T, alteration2: T) where T : GeneAlteration {
        assertThat(alteration1.gene).isEqualTo(alteration2.gene)
        assertThat(alteration1.geneRole).isEqualTo(alteration2.geneRole)
        assertThat(alteration1.proteinEffect).isEqualTo(alteration2.proteinEffect)
        assertThat(alteration1.isAssociatedWithDrugResistance).isEqualTo(alteration2.isAssociatedWithDrugResistance)
        if (alteration1 is VariantAlteration && alteration2 is VariantAlteration) {
            assertThat(alteration1.isCancerAssociatedVariant).isEqualTo(alteration2.isCancerAssociatedVariant)
        }
    }
}