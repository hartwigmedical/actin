package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
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
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableOrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.datamodel.linx.FusionDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.linx.FusionType;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxTestFactory;
import com.hartwig.actin.molecular.orange.datamodel.purple.GainLossInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleGainLoss;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleTestFactory;
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
    public void canFilterDisruptionsWithLosses() {
        String gene = "gene";

        OrangeRecord record1 = withDisruptionAndGainLoss(LinxTestFactory.disruptionBuilder().gene(gene).type("DEL").build(),
                PurpleTestFactory.gainLossBuilder().gene(gene).interpretation(GainLossInterpretation.FULL_LOSS).build());

        assertEquals(0, DriverExtraction.extract(record1).disruptions().size());

        OrangeRecord record2 = withDisruptionAndGainLoss(LinxTestFactory.disruptionBuilder().gene(gene).type("DUP").build(),
                PurpleTestFactory.gainLossBuilder().gene(gene).interpretation(GainLossInterpretation.FULL_LOSS).build());

        assertEquals(1, DriverExtraction.extract(record2).disruptions().size());

        OrangeRecord record3 = withDisruptionAndGainLoss(LinxTestFactory.disruptionBuilder().gene(gene).type("DEL").build(),
                PurpleTestFactory.gainLossBuilder().gene(gene).interpretation(GainLossInterpretation.FULL_GAIN).build());

        assertEquals(1, DriverExtraction.extract(record3).disruptions().size());

        OrangeRecord record4 = withDisruptionAndGainLoss(LinxTestFactory.disruptionBuilder().gene("other").type("DEL").build(),
                PurpleTestFactory.gainLossBuilder().gene(gene).interpretation(GainLossInterpretation.FULL_LOSS).build());

        assertEquals(1, DriverExtraction.extract(record4).disruptions().size());
    }

    @NotNull
    private static OrangeRecord withDisruptionAndGainLoss(@NotNull LinxDisruption disruption, @NotNull PurpleGainLoss gainLoss) {
        OrangeRecord base = TestOrangeFactory.createMinimalTestOrangeRecord();
        return ImmutableOrangeRecord.builder()
                .from(base)
                .linx(ImmutableLinxRecord.builder().from(base.linx()).addDisruptions(disruption).build())
                .purple(ImmutablePurpleRecord.builder().from(base.purple()).addGainsLosses(gainLoss).build())
                .build();
    }

    @Test
    public void canRemoveDriversInCaseOfNoTumorCells() {
        OrangeRecord base = TestOrangeFactory.createProperTestOrangeRecord();
        OrangeRecord orange = ImmutableOrangeRecord.builder()
                .from(base)
                .purple(ImmutablePurpleRecord.builder().from(base.purple()).containsTumorCells(false).build())
                .build();

        MolecularDrivers drivers = DriverExtraction.extract(orange);

        assertTrue(drivers.variants().isEmpty());
        assertTrue(drivers.amplifications().isEmpty());
        assertTrue(drivers.losses().isEmpty());
        assertTrue(drivers.homozygousDisruptions().isEmpty());
        assertTrue(drivers.disruptions().isEmpty());
        assertTrue(drivers.fusions().isEmpty());
        assertTrue(drivers.viruses().isEmpty());
    }

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
        assertEquals(DriverLikelihood.HIGH, variant.driverLikelihood());
        assertEquals("BRAF", variant.gene());
        assertEquals(4.1, variant.variantCopyNumber(), EPSILON);
        assertEquals(6.0, variant.totalCopyNumber(), EPSILON);
        assertEquals(0.98, variant.clonalLikelihood(), EPSILON);
    }

    private static void assertAmplifications(@NotNull Set<Amplification> amplifications) {
        assertEquals(1, amplifications.size());

        Amplification amplification = amplifications.iterator().next();
        assertEquals(DriverLikelihood.HIGH, amplification.driverLikelihood());
        assertEquals("MYC", amplification.gene());
        assertEquals(38, amplification.minCopies());
    }

    private static void assertLosses(@NotNull Set<Loss> losses) {
        assertEquals(1, losses.size());

        Loss loss = losses.iterator().next();
        assertEquals(DriverLikelihood.HIGH, loss.driverLikelihood());
        assertEquals("PTEN", loss.gene());
    }

    private static void assertHomozygousDisruptions(@NotNull Set<HomozygousDisruption> homozygousDisruptions) {
        assertEquals(1, homozygousDisruptions.size());

        HomozygousDisruption homozygousDisruption = homozygousDisruptions.iterator().next();
        assertEquals(DriverLikelihood.HIGH, homozygousDisruption.driverLikelihood());
        assertEquals("TP53", homozygousDisruption.gene());
    }

    private static void assertDisruptions(@NotNull Set<Disruption> disruptions) {
        assertEquals(1, disruptions.size());

        Disruption disruption = disruptions.iterator().next();
        assertEquals(DriverLikelihood.LOW, disruption.driverLikelihood());
        assertEquals("RB1", disruption.gene());
        assertEquals("DEL", disruption.type());
        assertEquals(0.8, disruption.junctionCopyNumber(), EPSILON);
        assertEquals(2.1, disruption.undisruptedCopyNumber(), EPSILON);
    }

    private static void assertFusions(@NotNull Set<Fusion> fusions) {
        assertEquals(1, fusions.size());

        Fusion fusion = fusions.iterator().next();
        assertEquals(DriverLikelihood.HIGH, fusion.driverLikelihood());
        assertEquals("EML4", fusion.geneStart());
        assertEquals("ALK", fusion.geneEnd());
        assertEquals(FusionDriverType.KNOWN_PAIR, fusion.driverType());
    }

    private static void assertViruses(@NotNull Set<Virus> viruses) {
        assertEquals(1, viruses.size());

        Virus virus = viruses.iterator().next();
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