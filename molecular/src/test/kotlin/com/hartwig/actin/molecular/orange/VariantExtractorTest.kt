package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.VariantEffect
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.hmftools.datamodel.purple.HotspotType
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleRecord
import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect
import com.hartwig.hmftools.datamodel.purple.PurpleDriver
import com.hartwig.hmftools.datamodel.purple.PurpleDriverType
import com.hartwig.hmftools.datamodel.purple.PurpleRecord
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.hmftools.datamodel.purple.PurpleVariantEffect
import com.hartwig.hmftools.datamodel.purple.PurpleVariantType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test

private const val EPSILON = 1.0E-10

class VariantExtractorTest {

    private val extractor = VariantExtractor(TestGeneFilterFactory.createAlwaysValid())

    @Test
    fun `Should extract set of variants successfully`() {
        val driver1: PurpleDriver = TestPurpleFactory.driverBuilder()
            .gene("gene 1")
            .transcript("ENST-canonical")
            .type(PurpleDriverType.MUTATION)
            .driverLikelihood(0.1)
            .build()

        val driver2: PurpleDriver = TestPurpleFactory.driverBuilder()
            .gene("gene 1")
            .transcript("ENST-canonical")
            .type(PurpleDriverType.GERMLINE_MUTATION)
            .driverLikelihood(0.6)
            .build()
        val driver3: PurpleDriver = TestPurpleFactory.driverBuilder()
            .gene("gene 1")
            .transcript("ENST-weird")
            .type(PurpleDriverType.GERMLINE_MUTATION)
            .driverLikelihood(0.9)
            .build()

        val purpleVariant1: PurpleVariant = TestPurpleFactory.variantBuilder()
            .type(PurpleVariantType.MNP)
            .gene("gene 1")
            .variantCopyNumber(0.4)
            .adjustedCopyNumber(0.8)
            .biallelic(false)
            .hotspot(HotspotType.NON_HOTSPOT)
            .subclonalLikelihood(0.3)
            .localPhaseSets(listOf(1))
            .canonicalImpact(
                TestPurpleFactory.transcriptImpactBuilder().transcript("ENST-canonical")
                    .hgvsCodingImpact("canonical hgvs coding")
                    .hgvsProteinImpact("canonical hgvs protein")
                    .affectedCodon(2)
                    .affectedExon(3)
                    .inSpliceRegion(false)
                    .addEffects(PurpleVariantEffect.MISSENSE)
                    .codingEffect(PurpleCodingEffect.MISSENSE)
                    .reported(true)
                    .build()
            )
            .addOtherImpacts(
                TestPurpleFactory.transcriptImpactBuilder().transcript("ENST-other")
                    .hgvsCodingImpact("other hgvs coding")
                    .hgvsProteinImpact("other hgvs protein")
                    .affectedCodon(null)
                    .affectedExon(null)
                    .inSpliceRegion(true)
                    .addEffects(PurpleVariantEffect.SPLICE_DONOR, PurpleVariantEffect.SYNONYMOUS)
                    .codingEffect(PurpleCodingEffect.SPLICE)
                    .build()
            )
            .build()

        val purpleVariant2: PurpleVariant = TestPurpleFactory.variantBuilder()
            .gene("gene 2")
            .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder()
                .codingEffect(PurpleCodingEffect.NONE)
                .build())
            .build()
        val purple: PurpleRecord = ImmutablePurpleRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
            .addSomaticDrivers(driver1, driver2, driver3)
            .addAllSomaticVariants(purpleVariant1, purpleVariant2)
            .build()

        val geneFilter = TestGeneFilterFactory.createValidForGenes(purpleVariant1.gene(), purpleVariant2.gene())
        val variantExtractor = VariantExtractor(geneFilter)

        val variants = variantExtractor.extract(purple)
        assertThat(variants).hasSize(1)

        val variant = variants.find { it.gene == "gene 1" }!!
        assertThat(variant.isReportable).isTrue
        assertThat(variant.driverLikelihood).isEqualTo(DriverLikelihood.MEDIUM)
        assertThat(variant.type).isEqualTo(VariantType.MNV)
        assertThat(variant.extendedVariantDetails?.variantCopyNumber).isEqualTo(0.4, Offset.offset(EPSILON))
        assertThat(variant.extendedVariantDetails?.totalCopyNumber).isEqualTo(0.8, Offset.offset(EPSILON))
        assertThat(variant.extendedVariantDetails?.isBiallelic).isFalse
        assertThat(variant.isCancerAssociatedVariant).isFalse
        assertThat(variant.extendedVariantDetails?.clonalLikelihood).isEqualTo(0.7, Offset.offset(EPSILON))
        assertThat(variant.extendedVariantDetails?.phaseGroups).isEqualTo(setOf(1))

        val canonical = variant.canonicalImpact
        assertThat(canonical.transcriptId).isEqualTo("ENST-canonical")
        assertThat(canonical.hgvsCodingImpact).isEqualTo("canonical hgvs coding")
        assertThat(canonical.hgvsProteinImpact).isEqualTo("canonical hgvs protein")
        assertThat(canonical.affectedCodon).isEqualTo(2)
        assertThat(canonical.affectedExon).isEqualTo(3)
        assertThat(canonical.inSpliceRegion).isFalse
        assertThat(canonical.effects.contains(VariantEffect.MISSENSE)).isTrue
        assertThat(canonical.codingEffect).isEqualTo(CodingEffect.MISSENSE)
        assertThat(variant.otherImpacts).hasSize(1)

        val other = variant.otherImpacts.iterator().next()
        assertThat(other.transcriptId).isEqualTo("ENST-other")
        assertThat(other.hgvsCodingImpact).isEqualTo("other hgvs coding")
        assertThat(other.hgvsProteinImpact).isEqualTo("other hgvs protein")
        assertThat(other.affectedCodon).isNull()
        assertThat(other.affectedExon).isNull()
        assertThat(other.inSpliceRegion).isTrue
        assertThat(other.effects.contains(VariantEffect.SPLICE_DONOR)).isTrue
        assertThat(other.effects.contains(VariantEffect.SYNONYMOUS)).isTrue
        assertThat(other.codingEffect).isEqualTo(CodingEffect.SPLICE)
    }

    @Test
    fun `Should retain ensembl transcripts only`() {
        val purpleVariant = TestPurpleFactory.variantBuilder()
            .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().reported(true).build())
            .addOtherImpacts(TestPurpleFactory.transcriptImpactBuilder().transcript("ENST-correct").build())
            .addOtherImpacts(TestPurpleFactory.transcriptImpactBuilder().transcript("weird one").build())
            .build()

        val purple = ImmutablePurpleRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
            .addAllSomaticVariants(purpleVariant)
            .build()
        val geneFilter = TestGeneFilterFactory.createValidForGenes(purpleVariant.gene())
        val variantExtractor = VariantExtractor(geneFilter)

        val variants = variantExtractor.extract(purple)
        assertThat(variants).hasSize(1)

        val variant = variants.iterator().next()
        assertThat(variant.otherImpacts).hasSize(1)
        assertThat(variant.otherImpacts.first().transcriptId).isEqualTo("ENST-correct")
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception when filtering reported variant`() {
        val purpleVariant: PurpleVariant = TestPurpleFactory.variantBuilder()
            .gene("gene 1")
            .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder()
                .codingEffect(PurpleCodingEffect.SPLICE)
                .reported(true)
                .build())
            .build()

        val purple: PurpleRecord = ImmutablePurpleRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
            .addAllSomaticVariants(purpleVariant)
            .build()

        val geneFilter = TestGeneFilterFactory.createValidForGenes("weird gene")
        val variantExtractor = VariantExtractor(geneFilter)
        variantExtractor.extract(purple)
    }

    @Test
    fun `Should determine correct type for all variant types`() {
        val mnp = TestPurpleFactory.variantBuilder().type(PurpleVariantType.MNP).build()
        assertThat(extractor.determineVariantType(mnp)).isEqualTo(VariantType.MNV)

        val snp = TestPurpleFactory.variantBuilder().type(PurpleVariantType.SNP).build()
        assertThat(extractor.determineVariantType(snp)).isEqualTo(VariantType.SNV)

        val insert = TestPurpleFactory.variantBuilder().type(PurpleVariantType.INDEL).ref("A").alt("AT").build()
        assertThat(extractor.determineVariantType(insert)).isEqualTo(VariantType.INSERT)

        val delete = TestPurpleFactory.variantBuilder().type(PurpleVariantType.INDEL).ref("AT").alt("A").build()
        assertThat(extractor.determineVariantType(delete)).isEqualTo(VariantType.DELETE)
    }

    @Test
    fun `Should correctly assess whether transcript is ensembl`() {
        val ensembl = TestPurpleFactory.transcriptImpactBuilder().transcript("ENST01").build()
        assertThat(extractor.isEnsemblTranscript(ensembl)).isTrue

        val nonEnsembl = TestPurpleFactory.transcriptImpactBuilder().transcript("something else").build()
        assertThat(extractor.isEnsemblTranscript(nonEnsembl)).isFalse
    }

    @Test
    fun `Should determine an effect for all variant effects`() {
        for (variantEffect in PurpleVariantEffect.values()) {
            assertThat(extractor.determineVariantEffect(variantEffect)).isNotNull()
        }
    }

    @Test
    fun `Should determine an effect for all defined coding effects`() {
        assertThat(extractor.determineCodingEffect(PurpleCodingEffect.UNDEFINED)).isNull()
        PurpleCodingEffect.values().filter { it != PurpleCodingEffect.UNDEFINED }
            .forEach { codingEffect ->
                assertThat(extractor.determineCodingEffect(codingEffect)).isNotNull()
            }
    }
}