package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.VariantEffect;
import com.hartwig.actin.molecular.datamodel.driver.VariantType;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCodingEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleDriver;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleDriverType;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleHotspotType;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleTranscriptImpact;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariantEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariantType;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class VariantExtractorTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void shouldExtractSetOfVariantsSuccessfully() {
        PurpleDriver driver1 = TestPurpleFactory.driverBuilder()
                .gene("gene 1")
                .transcript("ENST-canonical")
                .type(PurpleDriverType.MUTATION)
                .driverLikelihood(0.1)
                .build();
        PurpleDriver driver2 =
                TestPurpleFactory.driverBuilder().gene("gene 1").transcript("ENST-canonical").type(PurpleDriverType.GERMLINE_MUTATION)
                .driverLikelihood(0.6)
                .build();
        PurpleDriver driver3 = TestPurpleFactory.driverBuilder().gene("gene 1").transcript("ENST-weird")
                .type(PurpleDriverType.GERMLINE_MUTATION)
                .driverLikelihood(0.9)
                .build();

        PurpleVariant purpleVariant1 = TestPurpleFactory.variantBuilder()
                .reported(true)
                .type(PurpleVariantType.MNP)
                .gene("gene 1")
                .variantCopyNumber(0.4)
                .adjustedCopyNumber(0.8)
                .biallelic(false)
                .hotspot(PurpleHotspotType.NON_HOTSPOT)
                .subclonalLikelihood(0.3)
                .localPhaseSets(Lists.newArrayList(1))
                .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().transcript("ENST-canonical")
                        .hgvsCodingImpact("canonical hgvs coding")
                        .hgvsProteinImpact("canonical hgvs protein")
                        .affectedCodon(2)
                        .affectedExon(3)
                        .spliceRegion(false)
                        .addEffects(PurpleVariantEffect.MISSENSE)
                        .codingEffect(PurpleCodingEffect.MISSENSE)
                        .build())
                .addOtherImpacts(TestPurpleFactory.transcriptImpactBuilder().transcript("ENST-other")
                        .hgvsCodingImpact("other hgvs coding")
                        .hgvsProteinImpact("other hgvs protein")
                        .affectedCodon(null)
                        .affectedExon(null)
                        .spliceRegion(true)
                        .addEffects(PurpleVariantEffect.SPLICE_DONOR, PurpleVariantEffect.SYNONYMOUS)
                        .codingEffect(PurpleCodingEffect.SPLICE)
                        .build())
                .build();

        PurpleVariant purpleVariant2 = TestPurpleFactory.variantBuilder()
                .reported(false)
                .gene("gene 2")
                .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().codingEffect(PurpleCodingEffect.NONE).build())
                .build();

        PurpleRecord purple = ImmutablePurpleRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
                .addDrivers(driver1, driver2, driver3)
                .addVariants(purpleVariant1, purpleVariant2)
                .build();

        GeneFilter geneFilter = TestGeneFilterFactory.createValidForGenes(purpleVariant1.gene(), purpleVariant2.gene());
        VariantExtractor variantExtractor = new VariantExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase());

        Set<Variant> variants = variantExtractor.extract(purple);
        assertEquals(1, variants.size());

        Variant variant = findByGene(variants, "gene 1");
        assertTrue(variant.isReportable());
        assertEquals(DriverLikelihood.MEDIUM, variant.driverLikelihood());
        assertEquals(VariantType.MNV, variant.type());
        assertEquals(0.4, variant.variantCopyNumber(), EPSILON);
        assertEquals(0.8, variant.totalCopyNumber(), EPSILON);
        assertFalse(variant.isBiallelic());
        assertFalse(variant.isHotspot());
        assertEquals(0.7, variant.clonalLikelihood(), EPSILON);
        assertEquals(Sets.newHashSet(1), variant.phaseGroups());

        TranscriptImpact canonical = variant.canonicalImpact();
        assertEquals("ENST-canonical", canonical.transcriptId());
        assertEquals("canonical hgvs coding", canonical.hgvsCodingImpact());
        assertEquals("canonical hgvs protein", canonical.hgvsProteinImpact());
        assertEquals(2, (int) canonical.affectedCodon());
        assertEquals(3, (int) canonical.affectedExon());
        assertFalse(canonical.isSpliceRegion());
        assertTrue(canonical.effects().contains(VariantEffect.MISSENSE));
        assertEquals(CodingEffect.MISSENSE, canonical.codingEffect());

        assertEquals(1, variant.otherImpacts().size());
        TranscriptImpact other = variant.otherImpacts().iterator().next();
        assertEquals("ENST-other", other.transcriptId());
        assertEquals("other hgvs coding", other.hgvsCodingImpact());
        assertEquals("other hgvs protein", other.hgvsProteinImpact());
        assertNull(other.affectedCodon());
        assertNull(other.affectedExon());
        assertTrue(other.isSpliceRegion());
        assertTrue(other.effects().contains(VariantEffect.SPLICE_DONOR));
        assertTrue(other.effects().contains(VariantEffect.SYNONYMOUS));
        assertEquals(CodingEffect.SPLICE, other.codingEffect());
    }

    @Test
    public void shouldRetainEnsemblTranscriptsOnly() {
        PurpleVariant purpleVariant = TestPurpleFactory.variantBuilder()
                .reported(true)
                .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().build())
                .addOtherImpacts(TestPurpleFactory.transcriptImpactBuilder().transcript("ENST-correct").build())
                .addOtherImpacts(TestPurpleFactory.transcriptImpactBuilder().transcript("weird one").build())
                .build();

        PurpleRecord purple = ImmutablePurpleRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
                .addVariants(purpleVariant)
                .build();

        GeneFilter geneFilter = TestGeneFilterFactory.createValidForGenes(purpleVariant.gene());
        VariantExtractor variantExtractor = new VariantExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase());

        Set<Variant> variants = variantExtractor.extract(purple);
        assertEquals(1, variants.size());

        Variant variant = variants.iterator().next();
        assertEquals(1, variant.otherImpacts().size());
        assertEquals("ENST-correct", variant.otherImpacts().iterator().next().transcriptId());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenFilteringReportedVariant() {
        PurpleVariant purpleVariant = TestPurpleFactory.variantBuilder()
                .reported(true)
                .gene("gene 1")
                .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().codingEffect(PurpleCodingEffect.SPLICE).build())
                .build();

        PurpleRecord purple = ImmutablePurpleRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord().purple())
                .addVariants(purpleVariant)
                .build();

        GeneFilter geneFilter = TestGeneFilterFactory.createValidForGenes("weird gene");
        VariantExtractor variantExtractor = new VariantExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase());
        variantExtractor.extract(purple);
    }

    @Test
    public void shouldDetermineCorrectTypeForAllVariantTypes() {
        PurpleVariant mnp = TestPurpleFactory.variantBuilder().type(PurpleVariantType.MNP).build();
        assertEquals(VariantType.MNV, VariantExtractor.determineVariantType(mnp));

        PurpleVariant snp = TestPurpleFactory.variantBuilder().type(PurpleVariantType.SNP).build();
        assertEquals(VariantType.SNV, VariantExtractor.determineVariantType(snp));

        PurpleVariant insert = TestPurpleFactory.variantBuilder().type(PurpleVariantType.INDEL).ref("A").alt("AT").build();
        assertEquals(VariantType.INSERT, VariantExtractor.determineVariantType(insert));

        PurpleVariant delete = TestPurpleFactory.variantBuilder().type(PurpleVariantType.INDEL).ref("AT").alt("A").build();
        assertEquals(VariantType.DELETE, VariantExtractor.determineVariantType(delete));
    }

    @Test
    public void shouldDetermineDriverLikelihoodForAllPurpleDriverLikelihoods() {
        assertNull(VariantExtractor.determineDriverLikelihood(null));

        assertEquals(DriverLikelihood.HIGH, VariantExtractor.determineDriverLikelihood(withDriverLikelihood(1D)));
        assertEquals(DriverLikelihood.HIGH, VariantExtractor.determineDriverLikelihood(withDriverLikelihood(0.8)));
        assertEquals(DriverLikelihood.MEDIUM, VariantExtractor.determineDriverLikelihood(withDriverLikelihood(0.5)));
        assertEquals(DriverLikelihood.MEDIUM, VariantExtractor.determineDriverLikelihood(withDriverLikelihood(0.2)));
        assertEquals(DriverLikelihood.LOW, VariantExtractor.determineDriverLikelihood(withDriverLikelihood(0D)));
    }

    @NotNull
    private static PurpleDriver withDriverLikelihood(double driverLikelihood) {
        return TestPurpleFactory.driverBuilder().driverLikelihood(driverLikelihood).build();
    }

    @Test
    public void shouldCorrectlyAssessWhetherTranscriptIsEnsembl() {
        PurpleTranscriptImpact ensembl = TestPurpleFactory.transcriptImpactBuilder().transcript("ENST01").build();
        assertTrue(VariantExtractor.isEnsemblTranscript(ensembl));

        PurpleTranscriptImpact nonEnsembl = TestPurpleFactory.transcriptImpactBuilder().transcript("something else").build();
        assertFalse(VariantExtractor.isEnsemblTranscript(nonEnsembl));
    }

    @Test
    public void shouldDetermineAnEffectForAllVariantEffects() {
        for (PurpleVariantEffect variantEffect : PurpleVariantEffect.values()) {
            assertNotNull(VariantExtractor.determineVariantEffect(variantEffect));
        }
    }

    @Test
    public void shouldDetermineAnEffectForAllDefinedCodingEffects() {
        assertNull(VariantExtractor.determineCodingEffect(PurpleCodingEffect.UNDEFINED));

        for (PurpleCodingEffect codingEffect : PurpleCodingEffect.values()) {
            if (codingEffect != PurpleCodingEffect.UNDEFINED) {
                assertNotNull(VariantExtractor.determineCodingEffect(codingEffect));
            }
        }
    }

    @NotNull
    private static Variant findByGene(@NotNull Set<Variant> variants, @NotNull String geneToFind) {
        for (Variant variant : variants) {
            if (variant.gene().equals(geneToFind)) {
                return variant;
            }
        }

        throw new IllegalStateException("Could not find variant with gene: " + geneToFind);
    }
}