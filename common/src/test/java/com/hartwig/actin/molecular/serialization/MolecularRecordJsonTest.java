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
import com.hartwig.actin.molecular.datamodel.TestMolecularDataFactory;
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
import com.hartwig.actin.molecular.datamodel.driver.VariantDriverType;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceEntry;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.mapping.FusionGene;
import com.hartwig.actin.molecular.datamodel.mapping.GeneMutation;
import com.hartwig.actin.molecular.datamodel.mapping.MappedActinEvents;
import com.hartwig.actin.molecular.datamodel.pharmaco.Haplotype;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class MolecularRecordJsonTest {

    private static final String MOLECULAR_DIRECTORY = Resources.getResource("molecular").getPath();
    private static final String MOLECULAR_JSON = MOLECULAR_DIRECTORY + File.separator + "sample.molecular.json";

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canConvertBackAndForthJson() {
        MolecularRecord minimal = TestMolecularDataFactory.createMinimalTestMolecularRecord();
        MolecularRecord convertedMinimal = MolecularRecordJson.fromJson(MolecularRecordJson.toJson(minimal));

        assertEquals(minimal, convertedMinimal);

        MolecularRecord proper = TestMolecularDataFactory.createProperTestMolecularRecord();
        MolecularRecord convertedProper = MolecularRecordJson.fromJson(MolecularRecordJson.toJson(proper));

        assertEquals(proper, convertedProper);

        MolecularRecord exhaustive = TestMolecularDataFactory.createExhaustiveTestMolecularRecord();
        MolecularRecord convertedExhaustive = MolecularRecordJson.fromJson(MolecularRecordJson.toJson(exhaustive));

        assertEquals(exhaustive, convertedExhaustive);
    }

    @Test
    public void canReadMolecularJson() throws IOException {
        MolecularRecord molecular = MolecularRecordJson.read(MOLECULAR_JSON);

        assertEquals("ACTN01029999T", molecular.sampleId());
        assertEquals(ExperimentType.WGS, molecular.type());
        assertEquals(LocalDate.of(2021, 2, 23), molecular.date());
        assertTrue(molecular.hasReliableQuality());

        assertCharacteristics(molecular.characteristics());
        assertDrivers(molecular.drivers());
        assertPharmaco(molecular.pharmaco());
        assertEvidence(molecular.evidence());
        assertMappedEvents(molecular.mappedEvents());
    }

    private static void assertCharacteristics(@NotNull MolecularCharacteristics characteristics) {
        assertEquals(0.98, characteristics.purity(), EPSILON);
        assertTrue(characteristics.hasReliablePurity());

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
        assertEquals("BRAF V600E", variant.event());
        assertEquals(DriverLikelihood.HIGH, variant.driverLikelihood());
        assertEquals("BRAF", variant.gene());
        assertEquals("p.V600E", variant.impact());
        assertEquals(4.1, variant.variantCopyNumber(), EPSILON);
        assertEquals(6.0, variant.totalCopyNumber(), EPSILON);
        assertEquals(VariantDriverType.HOTSPOT, variant.driverType());
        assertEquals(1.0, variant.clonalLikelihood(), EPSILON);

        assertEquals(1, drivers.amplifications().size());
        Amplification amplification = drivers.amplifications().iterator().next();
        assertEquals("MYC amp", amplification.event());
        assertEquals(DriverLikelihood.HIGH, amplification.driverLikelihood());
        assertEquals("MYC", amplification.gene());
        assertEquals(38, amplification.copies());
        assertFalse(amplification.isPartial());

        assertEquals(1, drivers.losses().size());
        Loss loss = drivers.losses().iterator().next();
        assertEquals("PTEN del", loss.event());
        assertEquals(DriverLikelihood.HIGH, loss.driverLikelihood());
        assertEquals("PTEN", loss.gene());
        assertTrue(loss.isPartial());

        assertEquals(1, drivers.homozygousDisruptions().size());
        HomozygousDisruption homozygousDisruption = drivers.homozygousDisruptions().iterator().next();
        assertEquals("PTEN disruption", homozygousDisruption.event());
        assertEquals(DriverLikelihood.HIGH, homozygousDisruption.driverLikelihood());
        assertEquals("PTEN", homozygousDisruption.gene());

        assertEquals(2, drivers.disruptions().size());
        Disruption disruption1 = findByRange(drivers.disruptions(), "Intron 1 downstream");
        assertEquals(Strings.EMPTY, disruption1.event());
        assertEquals(DriverLikelihood.LOW, disruption1.driverLikelihood());
        assertEquals("NF1", disruption1.gene());
        assertEquals("DEL", disruption1.type());
        assertEquals(1.1, disruption1.junctionCopyNumber(), EPSILON);
        assertEquals(2.0, disruption1.undisruptedCopyNumber(), EPSILON);

        Disruption disruption2 = findByRange(drivers.disruptions(), "Intron 2 upstream");
        assertEquals(Strings.EMPTY, disruption2.event());
        assertEquals(DriverLikelihood.LOW, disruption2.driverLikelihood());
        assertEquals("NF1", disruption2.gene());
        assertEquals("DUP", disruption2.type());
        assertEquals(0.3, disruption2.junctionCopyNumber(), EPSILON);
        assertEquals(2.8, disruption2.undisruptedCopyNumber(), EPSILON);

        assertEquals(1, drivers.fusions().size());
        Fusion fusion = drivers.fusions().iterator().next();
        assertEquals("EML4-ALK fusion", fusion.event());
        assertEquals(DriverLikelihood.HIGH, fusion.driverLikelihood());
        assertEquals("EML4", fusion.fiveGene());
        assertEquals("ALK", fusion.threeGene());
        assertEquals("Exon 2 - Exon 4", fusion.details());
        assertEquals(FusionDriverType.KNOWN, fusion.driverType());

        assertEquals(1, drivers.viruses().size());
        Virus virus = drivers.viruses().iterator().next();
        assertEquals("HPV positive", virus.event());
        assertEquals(DriverLikelihood.HIGH, virus.driverLikelihood());
        assertEquals("Human papillomavirus type 16", virus.name());
        assertEquals(3, virus.integrations());
    }

    @NotNull
    private static Disruption findByRange(@NotNull Iterable<Disruption> disruptions, @NotNull String detailsToFind) {
        for (Disruption disruption : disruptions) {
            if (disruption.range().equals(detailsToFind)) {
                return disruption;
            }
        }

        throw new IllegalStateException("Could not find disruption with details: " + detailsToFind);
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

    private static void assertEvidence(@NotNull MolecularEvidence evidence) {
        assertEquals(3, evidence.actinTrials().size());
        EvidenceEntry actinTrial1 = findByEvent(evidence.actinTrials(), "High TML");
        assertEquals("Trial 1", actinTrial1.treatment());
        EvidenceEntry actinTrial2 = findByEvent(evidence.actinTrials(), "HR deficiency");
        assertEquals("Trial 2", actinTrial2.treatment());
        EvidenceEntry actinTrial3 = findByEvent(evidence.actinTrials(), "NF1 disruption");
        assertEquals("Trial 3", actinTrial3.treatment());

        assertEquals("trials", evidence.externalTrialSource());
        assertEquals(1, evidence.externalTrials().size());
        EvidenceEntry externalTrial = findByEvent(evidence.externalTrials(), "High TML");
        assertEquals("Trial 1", externalTrial.treatment());

        assertEquals("evidence", evidence.evidenceSource());
        assertEquals(2, evidence.approvedEvidence().size());
        EvidenceEntry approvedEvidence1 = findByTreatment(evidence.approvedEvidence(), "Pembrolizumab");
        assertEquals("High TML", approvedEvidence1.event());
        EvidenceEntry approvedEvidence2 = findByTreatment(evidence.approvedEvidence(), "Nivolumab");
        assertEquals("High TML", approvedEvidence2.event());

        assertEquals(1, evidence.onLabelExperimentalEvidence().size());
        assertEquals("on-label experimental",
                findByTreatment(evidence.onLabelExperimentalEvidence(), "on-label experimental drug").event());

        assertEquals(1, evidence.offLabelExperimentalEvidence().size());
        assertEquals("off-label experimental",
                findByTreatment(evidence.offLabelExperimentalEvidence(), "off-label experimental drug").event());

        assertEquals(1, evidence.preClinicalEvidence().size());
        assertEquals("pre clinical", findByTreatment(evidence.preClinicalEvidence(), "no drug yet").event());

        assertEquals(1, evidence.knownResistanceEvidence().size());
        assertEquals("known resistance", findByTreatment(evidence.knownResistanceEvidence(), "known resistant drug").event());

        assertEquals(1, evidence.suspectResistanceEvidence().size());
        assertEquals("suspect resistance", findByTreatment(evidence.suspectResistanceEvidence(), "suspect resistant drug").event());
    }

    @NotNull
    private static EvidenceEntry findByEvent(@NotNull Iterable<EvidenceEntry> evidences, @NotNull String eventToFind) {
        for (EvidenceEntry evidence : evidences) {
            if (evidence.event().equals(eventToFind)) {
                return evidence;
            }
        }

        throw new IllegalStateException("Could not find evidence with event: " + eventToFind);
    }

    @NotNull
    private static EvidenceEntry findByTreatment(@NotNull Iterable<EvidenceEntry> evidences, @NotNull String treatmentToFind) {
        for (EvidenceEntry evidence : evidences) {
            if (evidence.treatment().equals(treatmentToFind)) {
                return evidence;
            }
        }

        throw new IllegalStateException("Could not find evidence with treatment: " + treatmentToFind);
    }

    private static void assertMappedEvents(@NotNull MappedActinEvents events) {
        assertEquals(1, events.mutations().size());
        GeneMutation geneMutation = events.mutations().iterator().next();
        assertEquals("BRAF", geneMutation.gene());
        assertEquals("V600E", geneMutation.mutation());

        assertEquals(1, events.activatedGenes().size());
        assertEquals("ACT", events.activatedGenes().iterator().next());

        assertEquals(2, events.inactivatedGenes().size());
        assertTrue(events.inactivatedGenes().contains("NF1"));
        assertTrue(events.inactivatedGenes().contains("PTEN"));

        assertEquals(1, events.amplifiedGenes().size());
        assertEquals("MYC", events.amplifiedGenes().iterator().next());

        assertEquals(1, events.wildtypeGenes().size());
        assertEquals("WILD", events.wildtypeGenes().iterator().next());

        assertEquals(1, events.fusions().size());
        FusionGene fusionGene = events.fusions().iterator().next();
        assertEquals("EML4", fusionGene.fiveGene());
        assertEquals("ALK", fusionGene.threeGene());
    }
}