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
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.VariantDriverType;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeDataFactory;
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
        MolecularDrivers drivers = DriverExtraction.extract(TestOrangeDataFactory.createProperTestOrangeRecord());

        assertVariants(drivers.variants());
        assertAmplifications(drivers.amplifications());
        assertLosses(drivers.losses());
        assertDisruptions(drivers.disruptions());
        assertFusions(drivers.fusions());
        assertViruses(drivers.viruses());
    }

    private static void assertVariants(@NotNull Set<Variant> variants) {
        assertEquals(1, variants.size());

        Variant variant = variants.iterator().next();
        assertEquals("BRAF", variant.gene());
        assertEquals("p.V600E", variant.impact());
        assertEquals(4.1, variant.variantCopyNumber(), EPSILON);
        assertEquals(6.0, variant.totalCopyNumber(), EPSILON);
        assertEquals(VariantDriverType.HOTSPOT, variant.driverType());
        assertEquals(1.0, variant.driverLikelihood(), EPSILON);
        assertEquals(0.98, variant.clonalLikelihood(), EPSILON);
    }

    private static void assertAmplifications(@NotNull Set<Amplification> amplifications) {
        assertEquals(1, amplifications.size());

        Amplification amplification = amplifications.iterator().next();
        assertEquals("MYC", amplification.gene());
        assertFalse(amplification.isPartial());
        assertEquals(38, amplification.copies());
    }

    private static void assertLosses(@NotNull Set<Loss> losses) {
        assertEquals(1, losses.size());

        Loss loss = losses.iterator().next();
        assertEquals("PTEN", loss.gene());
        assertTrue(loss.isPartial());
    }

    private static void assertDisruptions(@NotNull Set<Disruption> disruptions) {
        assertEquals(2, disruptions.size());

        Disruption disruption1 = findByGene(disruptions, "TP53");
        assertTrue(disruption1.isHomozygous());
        assertTrue(disruption1.details().isEmpty());

        Disruption disruption2 = findByGene(disruptions, "RB1");
        assertFalse(disruption2.isHomozygous());
        assertEquals("Intron 1 downstream", disruption2.details());
    }

    @NotNull
    private static Disruption findByGene(@NotNull Iterable<Disruption> disruptions, @NotNull String geneToFind) {
        for (Disruption disruption : disruptions) {
            if (disruption.gene().equals(geneToFind)) {
                return disruption;
            }
        }

        throw new IllegalStateException("Could not find disruption with gene: " + geneToFind);
    }

    private static void assertFusions(@NotNull Set<Fusion> fusions) {
        assertEquals(1, fusions.size());

        Fusion fusion = fusions.iterator().next();
        assertEquals("EML4", fusion.fiveGene());
        assertEquals("ALK", fusion.threeGene());
        assertEquals("Exon 2 - Exon 4", fusion.details());
        assertEquals(FusionDriverType.KNOWN, fusion.driverType());
        assertEquals(DriverLikelihood.HIGH, fusion.driverLikelihood());
    }

    private static void assertViruses(@NotNull Set<Virus> viruses) {
        assertEquals(1, viruses.size());

        Virus virus = viruses.iterator().next();
        assertEquals("HPV 16", virus.name());
        assertEquals("3 integrations detected", virus.details());
        assertEquals(DriverLikelihood.HIGH, virus.driverLikelihood());
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
    public static VirusInterpreterEntry createTestVirus() {
        return ImmutableVirusInterpreterEntry.builder()
                .name(Strings.EMPTY)
                .integrations(0)
                .driverLikelihood(VirusDriverLikelihood.LOW)
                .build();
    }
}