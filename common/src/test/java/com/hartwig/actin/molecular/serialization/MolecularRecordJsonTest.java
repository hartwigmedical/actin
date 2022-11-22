package com.hartwig.actin.molecular.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.RefGenomeVersion;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.CodingContext;
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.DisruptionType;
import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.datamodel.driver.RegionType;
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.VariantEffect;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.driver.VirusType;
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory;
import com.hartwig.actin.molecular.datamodel.immunology.HlaAllele;
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology;
import com.hartwig.actin.molecular.datamodel.pharmaco.Haplotype;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;

import org.jetbrains.annotations.NotNull;
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
    public void canReadMolecularJson() throws IOException {
        MolecularRecord molecular = MolecularRecordJson.read(MOLECULAR_JSON);

        assertEquals("ACTN01029999", molecular.patientId());
        assertEquals("ACTN01029999T", molecular.sampleId());
        assertEquals(ExperimentType.WGS, molecular.type());
        assertEquals(RefGenomeVersion.V37, molecular.refGenomeVersion());
        assertEquals(LocalDate.of(2021, 2, 23), molecular.date());
        assertEquals("kb", molecular.evidenceSource());
        assertEquals("trial kb", molecular.externalTrialSource());
        assertTrue(molecular.containsTumorCells());
        assertTrue(molecular.hasSufficientQuality());

        assertCharacteristics(molecular.characteristics());
        assertDrivers(molecular.drivers());
        assertImmunology(molecular.immunology());
        assertPharmaco(molecular.pharmaco());
    }

    private static void assertCharacteristics(@NotNull MolecularCharacteristics characteristics) {
        assertEquals(0.98, characteristics.purity(), EPSILON);
        assertEquals(3.1, characteristics.ploidy(), EPSILON);

        assertNotNull(characteristics.predictedTumorOrigin());
        assertEquals("Melanoma", characteristics.predictedTumorOrigin().tumorType());
        assertEquals(0.99, characteristics.predictedTumorOrigin().likelihood(), EPSILON);

        assertFalse(characteristics.isMicrosatelliteUnstable());
        assertNull(characteristics.microsatelliteEvidence());
        assertTrue(characteristics.isHomologousRepairDeficient());
        assertEquals(TestActionableEvidenceFactory.builder()
                .addExternalEligibleTrials("PARP trial")
                .addOnLabelExperimentalTreatments("PARP on label")
                .addOffLabelExperimentalTreatments("PARP off label")
                .build(), characteristics.homologousRepairEvidence());
        assertEquals(4.32, characteristics.tumorMutationalBurden(), EPSILON);
        assertTrue(characteristics.hasHighTumorMutationalBurden());
        assertEquals(TestActionableEvidenceFactory.withApprovedTreatment("Pembro"), characteristics.tumorMutationalBurdenEvidence());
        assertEquals(243, (int) characteristics.tumorMutationalLoad());
        assertTrue(characteristics.hasHighTumorMutationalLoad());
        assertNull(characteristics.tumorMutationalLoadEvidence());
    }

    private static void assertDrivers(@NotNull MolecularDrivers drivers) {
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
        assertTrue(variant.isReportable());
        assertEquals("BRAF V600E", variant.event());
        assertEquals(DriverLikelihood.HIGH, variant.driverLikelihood());
        assertEquals(TestActionableEvidenceFactory.builder()
                .addKnownResistantTreatments("Anti-BRAF known")
                .addSuspectResistantTreatments("Anti-BRAF suspect")
                .build(), variant.evidence());
        assertEquals("BRAF", variant.gene());
        assertEquals(GeneRole.ONCO, variant.geneRole());
        assertEquals(ProteinEffect.GAIN_OF_FUNCTION, variant.proteinEffect());
        assertTrue(variant.isAssociatedWithDrugResistance());
        assertEquals(4.1, variant.variantCopyNumber(), EPSILON);
        assertEquals(6.0, variant.totalCopyNumber(), EPSILON);
        assertFalse(variant.isBiallelic());
        assertTrue(variant.isHotspot());
        assertEquals(1.0, variant.clonalLikelihood(), EPSILON);
        assertEquals(2, (int) variant.phaseGroup());

        TranscriptImpact canonicalImpact = variant.canonicalImpact();
        assertEquals("ENST00000288602", canonicalImpact.transcriptId());
        assertEquals("c.1799T>A", canonicalImpact.hgvsCodingImpact());
        assertEquals("p.V600E", canonicalImpact.hgvsProteinImpact());
        assertEquals(600, (int) canonicalImpact.affectedCodon());
        assertNull(canonicalImpact.affectedExon());
        assertFalse(canonicalImpact.isSpliceRegion());
        assertEquals(Sets.newHashSet(VariantEffect.MISSENSE), canonicalImpact.effects());
        assertEquals(CodingEffect.MISSENSE, canonicalImpact.codingEffect());

        assertEquals(1, variant.otherImpacts().size());
        TranscriptImpact otherImpact = variant.otherImpacts().iterator().next();
        assertEquals("other trans", otherImpact.transcriptId());
        assertEquals("c.other", otherImpact.hgvsCodingImpact());
        assertEquals("p.V601K", otherImpact.hgvsProteinImpact());
        assertNull(otherImpact.affectedCodon());
        assertEquals(8, (int) otherImpact.affectedExon());
        assertFalse(otherImpact.isSpliceRegion());
        assertEquals(Sets.newHashSet(VariantEffect.MISSENSE, VariantEffect.SPLICE_ACCEPTOR), otherImpact.effects());
        assertNull(otherImpact.codingEffect());
    }

    private static void assertAmplifications(@NotNull Set<Amplification> amplifications) {
        assertEquals(1, amplifications.size());

        Amplification amplification = amplifications.iterator().next();
        assertTrue(amplification.isReportable());
        assertEquals("MYC amp", amplification.event());
        assertEquals(DriverLikelihood.HIGH, amplification.driverLikelihood());
        assertEquals(TestActionableEvidenceFactory.withPreClinicalTreatment("MYC pre-clinical"), amplification.evidence());
        assertEquals("MYC", amplification.gene());
        assertEquals(GeneRole.UNKNOWN, amplification.geneRole());
        assertEquals(ProteinEffect.UNKNOWN, amplification.proteinEffect());
        assertNull(amplification.isAssociatedWithDrugResistance());
        assertEquals(38, amplification.minCopies());
        assertEquals(39, amplification.maxCopies());
        assertFalse(amplification.isPartial());
    }

    private static void assertLosses(@NotNull Set<Loss> losses) {
        assertEquals(1, losses.size());

        Loss loss = losses.iterator().next();
        assertFalse(loss.isReportable());
        assertEquals("PTEN del", loss.event());
        assertNull(loss.driverLikelihood());
        assertEquals(TestActionableEvidenceFactory.createEmpty(), loss.evidence());
        assertEquals("PTEN", loss.gene());
        assertEquals(GeneRole.TSG, loss.geneRole());
        assertEquals(ProteinEffect.LOSS_OF_FUNCTION, loss.proteinEffect());
        assertFalse(loss.isAssociatedWithDrugResistance());
        assertEquals(0, loss.minCopies());
        assertEquals(2, loss.maxCopies());
        assertTrue(loss.isPartial());
    }

    private static void assertHomozygousDisruptions(@NotNull Set<HomozygousDisruption> homozygousDisruptions) {
        assertEquals(1, homozygousDisruptions.size());

        HomozygousDisruption homozygousDisruption = homozygousDisruptions.iterator().next();
        assertTrue(homozygousDisruption.isReportable());
        assertEquals("PTEN hom disruption", homozygousDisruption.event());
        assertEquals(DriverLikelihood.HIGH, homozygousDisruption.driverLikelihood());
        assertEquals(TestActionableEvidenceFactory.createEmpty(), homozygousDisruption.evidence());
        assertEquals("PTEN", homozygousDisruption.gene());
        assertEquals(GeneRole.TSG, homozygousDisruption.geneRole());
        assertEquals(ProteinEffect.LOSS_OF_FUNCTION, homozygousDisruption.proteinEffect());
        assertFalse(homozygousDisruption.isAssociatedWithDrugResistance());
    }

    private static void assertDisruptions(@NotNull Set<Disruption> disruptions) {
        assertEquals(2, disruptions.size());

        Disruption disruption1 = findByEvent(disruptions, "NF1 disruption 1");
        assertTrue(disruption1.isReportable());
        assertEquals(DriverLikelihood.LOW, disruption1.driverLikelihood());
        assertEquals(TestActionableEvidenceFactory.createEmpty(), disruption1.evidence());
        assertEquals("NF1", disruption1.gene());
        assertEquals(GeneRole.UNKNOWN, disruption1.geneRole());
        assertEquals(ProteinEffect.UNKNOWN, disruption1.proteinEffect());
        assertNull(disruption1.isAssociatedWithDrugResistance());
        assertEquals(DisruptionType.DEL, disruption1.type());
        assertEquals(1.1, disruption1.junctionCopyNumber(), EPSILON);
        assertEquals(2.0, disruption1.undisruptedCopyNumber(), EPSILON);
        assertEquals(RegionType.INTRONIC, disruption1.regionType());
        assertEquals(CodingContext.NON_CODING, disruption1.codingContext());
        assertEquals(1, disruption1.clusterGroup());

        Disruption disruption2 = findByEvent(disruptions, "NF1 disruption 2");
        assertFalse(disruption2.isReportable());
        assertEquals(DriverLikelihood.LOW, disruption2.driverLikelihood());
        assertEquals(TestActionableEvidenceFactory.createEmpty(), disruption2.evidence());
        assertEquals("NF1", disruption2.gene());
        assertEquals(GeneRole.UNKNOWN, disruption2.geneRole());
        assertEquals(ProteinEffect.NO_EFFECT, disruption2.proteinEffect());
        assertEquals(0.3, disruption2.junctionCopyNumber(), EPSILON);
        assertEquals(2.8, disruption2.undisruptedCopyNumber(), EPSILON);
        assertEquals(RegionType.EXONIC, disruption2.regionType());
        assertEquals(CodingContext.CODING, disruption2.codingContext());
        assertEquals(2, disruption2.clusterGroup());
    }

    private static void assertFusions(@NotNull Set<Fusion> fusions) {
        assertEquals(1, fusions.size());

        Fusion fusion = fusions.iterator().next();
        assertTrue(fusion.isReportable());
        assertEquals("EML4 - ALK fusion", fusion.event());
        assertEquals(DriverLikelihood.HIGH, fusion.driverLikelihood());
        assertEquals(TestActionableEvidenceFactory.createEmpty(), fusion.evidence());
        assertEquals("EML4", fusion.geneStart());
        assertEquals("ENST00000318522", fusion.geneTranscriptStart());
        assertEquals(12, fusion.fusedExonUp());
        assertEquals("ALK", fusion.geneEnd());
        assertEquals("ENST00000389048", fusion.geneTranscriptEnd());
        assertEquals(20, fusion.fusedExonDown());
        assertEquals(FusionDriverType.KNOWN_PAIR, fusion.driverType());
        assertEquals(ProteinEffect.UNKNOWN, fusion.proteinEffect());
        assertFalse(fusion.isAssociatedWithDrugResistance());
    }

    private static void assertViruses(@NotNull Set<Virus> viruses) {
        assertEquals(1, viruses.size());

        Virus virus = viruses.iterator().next();
        assertTrue(virus.isReportable());
        assertEquals("HPV positive", virus.event());
        assertEquals(DriverLikelihood.HIGH, virus.driverLikelihood());
        assertEquals(TestActionableEvidenceFactory.createEmpty(), virus.evidence());
        assertEquals("Human papillomavirus type 16", virus.name());
        assertEquals(VirusType.HUMAN_PAPILLOMA_VIRUS, virus.type());
        assertTrue(virus.isReliable());
        assertEquals(3, virus.integrations());
    }

    @NotNull
    private static <T extends Driver> T findByEvent(@NotNull Iterable<T> drivers, @NotNull String eventToFind) {
        for (T driver : drivers) {
            if (driver.event().equals(eventToFind)) {
                return driver;
            }
        }

        throw new IllegalStateException("Could not find driver with event: " + eventToFind);
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
}