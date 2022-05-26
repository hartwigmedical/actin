package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.VariantDriverType;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.datamodel.linx.FusionDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.linx.FusionType;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.VariantHotspot;
import com.hartwig.actin.molecular.orange.datamodel.virus.ImmutableVirusInterpreterEntry;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class DriverExtractionTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canExtractFromProperTestData() {
        MolecularDrivers drivers = DriverExtraction.extract(TestOrangeFactory.createProperTestOrangeRecord());

        assertVariants(drivers.variants());
        assertAmplifications(drivers.amplifications());
        assertLosses(drivers.losses());
        assertHomozygousDisruptions(drivers.homozygousDisruptions());
        assertDisruptions(drivers.disruptions());
        assertFusions(drivers.fusions());
        assertViruses(drivers.viruses());
    }

    private static void assertVariants(@NotNull Set<Variant> variants) {
        assertEquals(1, variants.size());

        Variant variant = variants.iterator().next();
        assertEquals("BRAF V600E", variant.event());
        assertEquals(DriverLikelihood.HIGH, variant.driverLikelihood());
        assertEquals("BRAF", variant.gene());
        assertEquals("p.V600E", variant.impact());
        assertEquals(4.1, variant.variantCopyNumber(), EPSILON);
        assertEquals(6.0, variant.totalCopyNumber(), EPSILON);
        assertEquals(VariantDriverType.HOTSPOT, variant.driverType());
        assertEquals(0.98, variant.clonalLikelihood(), EPSILON);
    }

    private static void assertAmplifications(@NotNull Set<Amplification> amplifications) {
        assertEquals(1, amplifications.size());

        Amplification amplification = amplifications.iterator().next();
        assertEquals("MYC amp", amplification.event());
        assertEquals(DriverLikelihood.HIGH, amplification.driverLikelihood());
        assertEquals("MYC", amplification.gene());
        assertFalse(amplification.isPartial());
        assertEquals(38, amplification.copies());
    }

    private static void assertLosses(@NotNull Set<Loss> losses) {
        assertEquals(1, losses.size());

        Loss loss = losses.iterator().next();
        assertEquals("PTEN del", loss.event());
        assertEquals(DriverLikelihood.HIGH, loss.driverLikelihood());
        assertEquals("PTEN", loss.gene());
        assertTrue(loss.isPartial());
    }

    private static void assertHomozygousDisruptions(@NotNull Set<HomozygousDisruption> homozygousDisruptions) {
        assertEquals(1, homozygousDisruptions.size());

        HomozygousDisruption homozygousDisruption = homozygousDisruptions.iterator().next();
        assertEquals("TP53 disruption", homozygousDisruption.event());
        assertEquals(DriverLikelihood.HIGH, homozygousDisruption.driverLikelihood());
        assertEquals("TP53", homozygousDisruption.gene());
    }

    private static void assertDisruptions(@NotNull Set<Disruption> disruptions) {
        assertEquals(1, disruptions.size());

        Disruption disruption = disruptions.iterator().next();
        assertTrue(disruption.event().isEmpty());
        assertEquals(DriverLikelihood.LOW, disruption.driverLikelihood());
        assertEquals("RB1", disruption.gene());
        assertEquals("DEL", disruption.type());
        assertEquals(0.8, disruption.junctionCopyNumber(), EPSILON);
        assertEquals(2.1, disruption.undisruptedCopyNumber(), EPSILON);
        assertEquals("Intron 1 downstream", disruption.range());
    }

    private static void assertFusions(@NotNull Set<Fusion> fusions) {
        assertEquals(1, fusions.size());

        Fusion fusion = fusions.iterator().next();
        assertEquals("EML4-ALK fusion", fusion.event());
        assertEquals(DriverLikelihood.HIGH, fusion.driverLikelihood());
        assertEquals("EML4", fusion.fiveGene());
        assertEquals("ALK", fusion.threeGene());
        assertEquals("Exon 2 -> Exon 4", fusion.details());
        assertEquals(FusionDriverType.KNOWN, fusion.driverType());
    }

    private static void assertViruses(@NotNull Set<Virus> viruses) {
        assertEquals(1, viruses.size());

        Virus virus = viruses.iterator().next();
        assertEquals("HPV positive", virus.event());
        assertEquals(DriverLikelihood.HIGH, virus.driverLikelihood());
        assertEquals("Human papillomavirus type 16", virus.name());
        assertEquals(3, virus.integrations());
    }

    @Test
    public void canExtractImpactForAllVariants() {
        PurpleVariant protein = ImmutablePurpleVariant.builder()
                .from(createTestVariant())
                .hgvsProteinImpact("p.Val600Glu")
                .hgvsCodingImpact("c.something")
                .effect("missense")
                .build();
        assertEquals("p.V600E", DriverExtraction.extractImpact(protein));

        PurpleVariant coding = ImmutablePurpleVariant.builder()
                .from(createTestVariant())
                .hgvsProteinImpact(Strings.EMPTY)
                .hgvsCodingImpact("c.something")
                .effect("missense")
                .build();
        assertEquals("c.something", DriverExtraction.extractImpact(coding));

        PurpleVariant effect = ImmutablePurpleVariant.builder()
                .from(createTestVariant())
                .hgvsProteinImpact(Strings.EMPTY)
                .hgvsCodingImpact(Strings.EMPTY)
                .effect("missense")
                .build();
        assertEquals("missense", DriverExtraction.extractImpact(effect));

        PurpleVariant upstream = ImmutablePurpleVariant.builder()
                .from(createTestVariant())
                .hgvsProteinImpact(Strings.EMPTY)
                .hgvsCodingImpact(Strings.EMPTY)
                .effect(DriverExtraction.UPSTREAM_GENE_VARIANT)
                .build();
        assertEquals("upstream", DriverExtraction.extractImpact(upstream));
    }

    @Test
    public void canInterpretDriverLikelihoods() {
        PurpleVariant high = ImmutablePurpleVariant.builder().from(createTestVariant()).driverLikelihood(1D).build();
        assertEquals(DriverLikelihood.HIGH, DriverExtraction.interpretDriverLikelihood(high));

        PurpleVariant medium = ImmutablePurpleVariant.builder().from(createTestVariant()).driverLikelihood(0.5).build();
        assertEquals(DriverLikelihood.MEDIUM, DriverExtraction.interpretDriverLikelihood(medium));

        PurpleVariant low = ImmutablePurpleVariant.builder().from(createTestVariant()).driverLikelihood(0D).build();
        assertEquals(DriverLikelihood.LOW, DriverExtraction.interpretDriverLikelihood(low));
    }

    @Test
    public void canExtractDriverTypeForAllVariants() {
        PurpleVariant hotspot =
                ImmutablePurpleVariant.builder().from(createTestVariant()).hotspot(VariantHotspot.HOTSPOT).biallelic(true).build();
        assertEquals(VariantDriverType.HOTSPOT, DriverExtraction.extractVariantDriverType(hotspot));

        PurpleVariant biallelic =
                ImmutablePurpleVariant.builder().from(createTestVariant()).hotspot(VariantHotspot.NON_HOTSPOT).biallelic(true).build();
        assertEquals(VariantDriverType.BIALLELIC, DriverExtraction.extractVariantDriverType(biallelic));

        PurpleVariant vus =
                ImmutablePurpleVariant.builder().from(createTestVariant()).hotspot(VariantHotspot.NON_HOTSPOT).biallelic(false).build();
        assertEquals(VariantDriverType.VUS, DriverExtraction.extractVariantDriverType(vus));
    }

    @Test
    public void canExtractFusionDriverTypeForAllFusions() {
        for (FusionType type : FusionType.values()) {
            if (type != FusionType.NONE) {
                LinxFusion fusion = ImmutableLinxFusion.builder().from(createTestFusion()).type(type).build();
                assertNotNull(DriverExtraction.extractFusionDriverType(fusion));
            }
        }
    }

    @Test
    public void canExtractDriverLikelihoodForAllFusions() {
        LinxFusion high = ImmutableLinxFusion.builder().from(createTestFusion()).driverLikelihood(FusionDriverLikelihood.HIGH).build();
        assertEquals(DriverLikelihood.HIGH, DriverExtraction.extractFusionDriverLikelihood(high));

        LinxFusion low = ImmutableLinxFusion.builder().from(createTestFusion()).driverLikelihood(FusionDriverLikelihood.LOW).build();
        assertEquals(DriverLikelihood.LOW, DriverExtraction.extractFusionDriverLikelihood(low));
    }

    @Test
    public void canExtractDriverLikelihoodForAllViruses() {
        VirusInterpreterEntry high =
                ImmutableVirusInterpreterEntry.builder().from(createTestVirus()).driverLikelihood(VirusDriverLikelihood.HIGH).build();
        assertEquals(DriverLikelihood.HIGH, DriverExtraction.extractVirusDriverLikelihood(high));

        VirusInterpreterEntry low =
                ImmutableVirusInterpreterEntry.builder().from(createTestVirus()).driverLikelihood(VirusDriverLikelihood.LOW).build();
        assertEquals(DriverLikelihood.LOW, DriverExtraction.extractVirusDriverLikelihood(low));

        VirusInterpreterEntry unknown =
                ImmutableVirusInterpreterEntry.builder().from(createTestVirus()).driverLikelihood(VirusDriverLikelihood.UNKNOWN).build();
        assertEquals(DriverLikelihood.LOW, DriverExtraction.extractVirusDriverLikelihood(unknown));
    }

    @Test
    public void canKeep3Digits() {
        assertEquals(3, DriverExtraction.keep3Digits(3D), EPSILON);
        assertEquals(3.123, DriverExtraction.keep3Digits(3.123), EPSILON);
        assertEquals(3.123, DriverExtraction.keep3Digits(3.123456789), EPSILON);
    }

    @NotNull
    private static PurpleVariant createTestVariant() {
        return ImmutablePurpleVariant.builder()
                .gene(Strings.EMPTY)
                .hgvsProteinImpact(Strings.EMPTY)
                .hgvsCodingImpact(Strings.EMPTY)
                .effect(Strings.EMPTY)
                .alleleCopyNumber(0D)
                .totalCopyNumber(0D)
                .hotspot(VariantHotspot.NON_HOTSPOT)
                .biallelic(false)
                .driverLikelihood(0D)
                .clonalLikelihood(0D)
                .build();
    }

    @NotNull
    private static LinxFusion createTestFusion() {
        return ImmutableLinxFusion.builder()
                .type(FusionType.NONE)
                .geneStart(Strings.EMPTY)
                .geneContextStart(Strings.EMPTY)
                .geneEnd(Strings.EMPTY)
                .geneContextEnd(Strings.EMPTY)
                .driverLikelihood(FusionDriverLikelihood.LOW)
                .build();
    }

    @NotNull
    private static VirusInterpreterEntry createTestVirus() {
        return ImmutableVirusInterpreterEntry.builder()
                .name(Strings.EMPTY)
                .integrations(0)
                .driverLikelihood(VirusDriverLikelihood.LOW)
                .build();
    }
}