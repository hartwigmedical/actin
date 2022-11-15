package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
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
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleRecord;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class DriverExtractionTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void removesDriversInCaseOfNoTumorCells() {
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
}