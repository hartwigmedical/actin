package com.hartwig.actin.molecular.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

import com.google.common.io.Resources;
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
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
import com.hartwig.actin.molecular.datamodel.immunology.HlaAllele;
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology;
import com.hartwig.actin.molecular.datamodel.pharmaco.Haplotype;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Ignore;
import org.junit.Test;

public class MolecularRecordJsonTest {

    private static final String MOLECULAR_DIRECTORY = Resources.getResource("molecular").getPath();
    private static final String MOLECULAR_JSON = MOLECULAR_DIRECTORY + File.separator + "sample.molecular.json";

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canConvertBackAndForthJson() {
        MolecularRecord minimal = TestMolecularFactory.createMinimalTestMolecularRecord();
        MolecularRecord convertedMinimal = MolecularRecordJson.fromJson(MolecularRecordJson.toJson(minimal));

        assertEquals(minimal, convertedMinimal);

        MolecularRecord proper = TestMolecularFactory.createProperTestMolecularRecord();
        MolecularRecord convertedProper = MolecularRecordJson.fromJson(MolecularRecordJson.toJson(proper));

        assertEquals(proper, convertedProper);

        MolecularRecord exhaustive = TestMolecularFactory.createExhaustiveTestMolecularRecord();
        MolecularRecord convertedExhaustive = MolecularRecordJson.fromJson(MolecularRecordJson.toJson(exhaustive));

        assertEquals(exhaustive, convertedExhaustive);
    }

    @Test
    @Ignore
    public void canReadMolecularJson() throws IOException {
        MolecularRecord molecular = MolecularRecordJson.read(MOLECULAR_JSON);

        assertEquals("ACTN01029999T", molecular.sampleId());
        assertEquals(ExperimentType.WGS, molecular.type());
        assertEquals(LocalDate.of(2021, 2, 23), molecular.date());
        assertTrue(molecular.containsTumorCells());
        assertTrue(molecular.hasSufficientQuality());

        assertCharacteristics(molecular.characteristics());
        assertDrivers(molecular.drivers());
        assertImmunology(molecular.immunology());
        assertPharmaco(molecular.pharmaco());
        assertWildTypeGenes(molecular.wildTypeGenes());
    }

    private static void assertCharacteristics(@NotNull MolecularCharacteristics characteristics) {
        assertEquals(0.98, characteristics.purity(), EPSILON);

        assertNotNull(characteristics.predictedTumorOrigin());
        assertEquals("Melanoma", characteristics.predictedTumorOrigin().tumorType());
        assertEquals(0.99, characteristics.predictedTumorOrigin().likelihood(), EPSILON);

        assertFalse(characteristics.isMicrosatelliteUnstable());
        assertTrue(characteristics.isHomologousRepairDeficient());
        assertEquals(4.32, characteristics.tumorMutationalBurden(), EPSILON);
        assertEquals(243, (int) characteristics.tumorMutationalLoad());
    }

    private static void assertDrivers(@NotNull MolecularDrivers drivers) {
        assertEquals(1, drivers.variants().size());
        Variant variant = drivers.variants().iterator().next();
        assertEquals(DriverLikelihood.HIGH, variant.driverLikelihood());
        assertEquals("BRAF", variant.gene());
        assertEquals(4.1, variant.variantCopyNumber(), EPSILON);
        assertEquals(6.0, variant.totalCopyNumber(), EPSILON);
        assertEquals(1.0, variant.clonalLikelihood(), EPSILON);

        assertEquals(1, drivers.amplifications().size());
        Amplification amplification = drivers.amplifications().iterator().next();
        assertEquals(DriverLikelihood.HIGH, amplification.driverLikelihood());
        assertEquals("MYC", amplification.gene());
        assertEquals(38, amplification.minCopies());

        assertEquals(1, drivers.losses().size());
        Loss loss = drivers.losses().iterator().next();
        assertEquals(DriverLikelihood.HIGH, loss.driverLikelihood());
        assertEquals("PTEN", loss.gene());

        assertEquals(1, drivers.homozygousDisruptions().size());
        HomozygousDisruption homozygousDisruption = drivers.homozygousDisruptions().iterator().next();
        assertEquals(DriverLikelihood.HIGH, homozygousDisruption.driverLikelihood());
        assertEquals("PTEN", homozygousDisruption.gene());

        assertEquals(2, drivers.disruptions().size());
        Disruption disruption1 = findByRange(drivers.disruptions(), "Intron 1 downstream");
        assertEquals(DriverLikelihood.LOW, disruption1.driverLikelihood());
        assertEquals("NF1", disruption1.gene());
        assertEquals("DEL", disruption1.type());
        assertEquals(1.1, disruption1.junctionCopyNumber(), EPSILON);
        assertEquals(2.0, disruption1.undisruptedCopyNumber(), EPSILON);

        Disruption disruption2 = findByRange(drivers.disruptions(), "Intron 2 upstream");
        assertEquals(DriverLikelihood.LOW, disruption2.driverLikelihood());
        assertEquals("NF1", disruption2.gene());
        assertEquals("DUP", disruption2.type());
        assertEquals(0.3, disruption2.junctionCopyNumber(), EPSILON);
        assertEquals(2.8, disruption2.undisruptedCopyNumber(), EPSILON);

        assertEquals(1, drivers.fusions().size());
        Fusion fusion = drivers.fusions().iterator().next();
        assertEquals(DriverLikelihood.HIGH, fusion.driverLikelihood());
        assertEquals("EML4", fusion.geneStart());
        assertEquals("ALK", fusion.geneEnd());
        assertEquals(FusionDriverType.KNOWN, fusion.driverType());

        assertEquals(1, drivers.viruses().size());
        Virus virus = drivers.viruses().iterator().next();
        assertEquals(DriverLikelihood.HIGH, virus.driverLikelihood());
        assertEquals("Human papillomavirus type 16", virus.name());
        assertEquals(3, virus.integrations());
    }

    @NotNull
    private static Disruption findByRange(@NotNull Iterable<Disruption> disruptions, @NotNull String rangeToFind) {
        for (Disruption disruption : disruptions) {
            if (disruption.range().equals(rangeToFind)) {
                return disruption;
            }
        }

        throw new IllegalStateException("Could not find disruption with range: " + rangeToFind);
    }

    private static void assertImmunology(@NotNull MolecularImmunology immunology) {
        assertEquals(1, immunology.hlaAlleles().size());
        HlaAllele hlaAllele = immunology.hlaAlleles().iterator().next();

        assertEquals("A*02:01", hlaAllele.name());
        assertEquals(1.2, hlaAllele.tumorCopyNumber(), EPSILON);
        assertFalse(hlaAllele.hasSomaticMutations());
    }

    private static void assertPharmaco(@NotNull Set<PharmacoEntry> pharmaco) {
        assertEquals(1, pharmaco.size());

        PharmacoEntry entry = pharmaco.iterator().next();
        assertEquals("DPYD", entry.gene());
        assertEquals(1, entry.haplotypes().size());

        Haplotype haplotype = entry.haplotypes().iterator().next();
        assertEquals("1* HOM", haplotype.name());
        assertEquals("Normal function", haplotype.function());
    }

    private static void assertWildTypeGenes(@Nullable Set<String> wildTypeGenes) {
        assertNotNull(wildTypeGenes);
        assertEquals(1, wildTypeGenes.size());
        assertEquals("KRAS", wildTypeGenes.iterator().next());
    }
}