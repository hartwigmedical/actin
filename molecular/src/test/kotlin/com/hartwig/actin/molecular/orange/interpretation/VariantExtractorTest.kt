package com.hartwig.actin.molecular.orange.interpretation

import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.molecular.datamodel.driver.VariantEffect
import com.hartwig.actin.molecular.datamodel.driver.VariantType
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory
import com.hartwig.hmftools.datamodel.purple.HotspotType
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleRecord
import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect
import com.hartwig.hmftools.datamodel.purple.PurpleDriver
import com.hartwig.hmftools.datamodel.purple.PurpleDriverType
import com.hartwig.hmftools.datamodel.purple.PurpleRecord
import com.hartwig.hmftools.datamodel.purple.PurpleTranscriptImpact
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.hmftools.datamodel.purple.PurpleVariantEffect
import com.hartwig.hmftools.datamodel.purple.PurpleVariantType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VariantExtractorTest {

    @Test
    fun shouldExtractSetOfVariantsSuccessfully() {
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
            .reported(true)
            .type(PurpleVariantType.MNP)
            .gene("gene 1")
            .variantCopyNumber(0.4)
            .adjustedCopyNumber(0.8)
            .biallelic(false)
            .hotspot(HotspotType.NON_HOTSPOT)
            .subclonalLikelihood(0.3)
            .localPhaseSets(Lists.newArrayList(1))
            .canonicalImpact(
                TestPurpleFactory.transcriptImpactBuilder().transcript("ENST-canonical")
                    .hgvsCodingImpact("canonical hgvs coding")
                    .hgvsProteinImpact("canonical hgvs protein")
                    .affectedCodon(2)
                    .affectedExon(3)
                    .inSpliceRegion(false)
                    .addEffects(PurpleVariantEffect.MISSENSE)
                    .codingEffect(PurpleCodingEffect.MISSENSE)
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
            .reported(false)
            .gene("gene 2")
            .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().codingEffect(PurpleCodingEffect.NONE).build())
            .build()
        val purple: PurpleRecord = ImmutablePurpleRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
            .addSomaticDrivers(driver1, driver2, driver3)
            .addAllSomaticVariants(purpleVariant1, purpleVariant2)
            .build()

        val geneFilter = TestGeneFilterFactory.createValidForGenes(purpleVariant1.gene(), purpleVariant2.gene())
        val variantExtractor = VariantExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase())

        val variants = variantExtractor.extract(purple)
        assertEquals(1, variants.size.toLong())

        val variant = findByGene(variants, "gene 1")
        assertTrue(variant.isReportable())
        assertEquals(DriverLikelihood.MEDIUM, variant.driverLikelihood())
        assertEquals(VariantType.MNV, variant.type())
        assertEquals(0.4, variant.variantCopyNumber(), EPSILON)
        assertEquals(0.8, variant.totalCopyNumber(), EPSILON)
        assertFalse(variant.isBiallelic())
        assertFalse(variant.isHotspot())
        assertEquals(0.7, variant.clonalLikelihood(), EPSILON)
        assertEquals(Sets.newHashSet(1), variant.phaseGroups())

        val canonical = variant.canonicalImpact()
        assertEquals("ENST-canonical", canonical.transcriptId())
        assertEquals("canonical hgvs coding", canonical.hgvsCodingImpact())
        assertEquals("canonical hgvs protein", canonical.hgvsProteinImpact())
        assertEquals(2, (canonical.affectedCodon() as Int).toLong())
        assertEquals(3, (canonical.affectedExon() as Int).toLong())
        assertFalse(canonical.isSpliceRegion())
        assertTrue(canonical.effects().contains(VariantEffect.MISSENSE))
        assertEquals(CodingEffect.MISSENSE, canonical.codingEffect())
        assertEquals(1, variant.otherImpacts().size.toLong())

        val other = variant.otherImpacts().iterator().next()
        assertEquals("ENST-other", other.transcriptId())
        assertEquals("other hgvs coding", other.hgvsCodingImpact())
        assertEquals("other hgvs protein", other.hgvsProteinImpact())
        assertNull(other.affectedCodon())
        assertNull(other.affectedExon())
        assertTrue(other.isSpliceRegion())
        assertTrue(other.effects().contains(VariantEffect.SPLICE_DONOR))
        assertTrue(other.effects().contains(VariantEffect.SYNONYMOUS))
        assertEquals(CodingEffect.SPLICE, other.codingEffect())
    }

    @Test
    fun shouldRetainEnsemblTranscriptsOnly() {
        val purpleVariant: PurpleVariant = TestPurpleFactory.variantBuilder()
            .reported(true)
            .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().build())
            .addOtherImpacts(TestPurpleFactory.transcriptImpactBuilder().transcript("ENST-correct").build())
            .addOtherImpacts(TestPurpleFactory.transcriptImpactBuilder().transcript("weird one").build())
            .build()
        val purple: PurpleRecord = ImmutablePurpleRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
            .addAllSomaticVariants(purpleVariant)
            .build()
        val geneFilter = TestGeneFilterFactory.createValidForGenes(purpleVariant.gene())
        val variantExtractor = VariantExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase())

        val variants = variantExtractor.extract(purple)
        assertEquals(1, variants.size.toLong())

        val variant = variants.iterator().next()
        assertEquals(1, variant.otherImpacts().size.toLong())
        assertEquals("ENST-correct", variant.otherImpacts().iterator().next().transcriptId())
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionWhenFilteringReportedVariant() {
        val purpleVariant: PurpleVariant = TestPurpleFactory.variantBuilder()
            .reported(true)
            .gene("gene 1")
            .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().codingEffect(PurpleCodingEffect.SPLICE).build())
            .build()
        val purple: PurpleRecord = ImmutablePurpleRecord.builder()
            .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
            .addAllSomaticVariants(purpleVariant)
            .build()
        val geneFilter = TestGeneFilterFactory.createValidForGenes("weird gene")
        val variantExtractor = VariantExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase())
        variantExtractor.extract(purple)
    }

    @Test
    fun shouldDetermineCorrectTypeForAllVariantTypes() {
        val mnp: PurpleVariant = TestPurpleFactory.variantBuilder().type(PurpleVariantType.MNP).build()
        assertEquals(VariantType.MNV, VariantExtractor.determineVariantType(mnp))

        val snp: PurpleVariant = TestPurpleFactory.variantBuilder().type(PurpleVariantType.SNP).build()
        assertEquals(VariantType.SNV, VariantExtractor.determineVariantType(snp))

        val insert: PurpleVariant = TestPurpleFactory.variantBuilder().type(PurpleVariantType.INDEL).ref("A").alt("AT").build()
        assertEquals(VariantType.INSERT, VariantExtractor.determineVariantType(insert))

        val delete: PurpleVariant = TestPurpleFactory.variantBuilder().type(PurpleVariantType.INDEL).ref("AT").alt("A").build()
        assertEquals(VariantType.DELETE, VariantExtractor.determineVariantType(delete))
    }

    @Test
    fun shouldDetermineDriverLikelihoodForAllPurpleDriverLikelihoods() {
        assertNull(VariantExtractor.determineDriverLikelihood(null))
        assertEquals(DriverLikelihood.HIGH, VariantExtractor.determineDriverLikelihood(withDriverLikelihood(1.0)))
        assertEquals(DriverLikelihood.HIGH, VariantExtractor.determineDriverLikelihood(withDriverLikelihood(0.8)))
        assertEquals(DriverLikelihood.MEDIUM, VariantExtractor.determineDriverLikelihood(withDriverLikelihood(0.5)))
        assertEquals(DriverLikelihood.MEDIUM, VariantExtractor.determineDriverLikelihood(withDriverLikelihood(0.2)))
        assertEquals(DriverLikelihood.LOW, VariantExtractor.determineDriverLikelihood(withDriverLikelihood(0.0)))
    }

    @Test
    fun shouldCorrectlyAssessWhetherTranscriptIsEnsembl() {
        val ensembl: PurpleTranscriptImpact = TestPurpleFactory.transcriptImpactBuilder().transcript("ENST01").build()
        assertTrue(VariantExtractor.isEnsemblTranscript(ensembl))

        val nonEnsembl: PurpleTranscriptImpact = TestPurpleFactory.transcriptImpactBuilder().transcript("something else").build()
        assertFalse(VariantExtractor.isEnsemblTranscript(nonEnsembl))
    }

    @Test
    fun shouldDetermineAnEffectForAllVariantEffects() {
        for (variantEffect in PurpleVariantEffect.values()) {
            assertNotNull(VariantExtractor.determineVariantEffect(variantEffect))
        }
    }

    @Test
    fun shouldDetermineAnEffectForAllDefinedCodingEffects() {
        assertNull(VariantExtractor.determineCodingEffect(PurpleCodingEffect.UNDEFINED))
        for (codingEffect in PurpleCodingEffect.values()) {
            if (codingEffect != PurpleCodingEffect.UNDEFINED) {
                assertNotNull(VariantExtractor.determineCodingEffect(codingEffect))
            }
        }
    }

    companion object {
        private const val EPSILON = 1.0E-10

        private fun withDriverLikelihood(driverLikelihood: Double): PurpleDriver {
            return TestPurpleFactory.driverBuilder().driverLikelihood(driverLikelihood).build()
        }

        private fun findByGene(variants: MutableSet<Variant>, geneToFind: String): Variant {
            for (variant in variants) {
                if (variant.gene() == geneToFind) {
                    return variant
                }
            }
            throw IllegalStateException("Could not find variant with gene: $geneToFind")
        }
    }
}