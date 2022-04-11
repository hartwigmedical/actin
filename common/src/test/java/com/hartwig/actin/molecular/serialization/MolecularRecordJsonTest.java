package com.hartwig.actin.molecular.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.google.common.io.Resources;
import com.hartwig.actin.molecular.datamodel.DriverEntry;
import com.hartwig.actin.molecular.datamodel.EvidenceEntry;
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.FusionGene;
import com.hartwig.actin.molecular.datamodel.GeneMutation;
import com.hartwig.actin.molecular.datamodel.InactivatedGene;
import com.hartwig.actin.molecular.datamodel.MappedActinEvents;
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.PharmacoEntry;
import com.hartwig.actin.molecular.datamodel.TestMolecularDataFactory;

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
        assertEquals("PASS", molecular.qc());

        assertCharacteristics(molecular.characteristics());
        assertDrivers(molecular.drivers());
        assertPharmaco(molecular.pharmaco());
        assertEvidence(molecular.evidence());
        assertMappedEvents(molecular.mappedEvents());
    }

    private static void assertCharacteristics(@NotNull MolecularCharacteristics characteristics) {
        assertEquals(0.5, characteristics.purity(), EPSILON);

        assertNotNull(characteristics.predictedTumorOrigin());
        assertEquals("Melanoma", characteristics.predictedTumorOrigin().tumorType());
        assertEquals(0.99, characteristics.predictedTumorOrigin().likelihood(), EPSILON);

        assertFalse(characteristics.isMicrosatelliteUnstable());
        assertTrue(characteristics.isHomologousRepairDeficient());
        assertEquals(4.32, characteristics.tumorMutationalBurden(), EPSILON);
        assertEquals(243, (int) characteristics.tumorMutationalLoad());
    }

    private static void assertDrivers(@NotNull List<DriverEntry> drivers) {
        assertTrue(drivers.isEmpty());
    }

    private static void assertPharmaco(@NotNull List<PharmacoEntry> pharmaco) {
        assertTrue(pharmaco.isEmpty());
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
        assertEquals(2, evidence.approvedResponsiveEvidence().size());
        EvidenceEntry approvedEvidence1 = findByTreatment(evidence.approvedResponsiveEvidence(), "Pembrolizumab");
        assertEquals("High TML", approvedEvidence1.event());
        EvidenceEntry approvedEvidence2 = findByTreatment(evidence.approvedResponsiveEvidence(), "Nivolumab");
        assertEquals("High TML", approvedEvidence2.event());

        assertTrue(evidence.experimentalResponsiveEvidence().isEmpty());
        assertTrue(evidence.otherResponsiveEvidence().isEmpty());
        assertTrue(evidence.resistanceEvidence().isEmpty());
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
        assertEquals("TP53", geneMutation.gene());
        assertEquals("exon 1", geneMutation.mutation());

        assertEquals(1, events.activatedGenes().size());
        assertEquals("ACT", events.activatedGenes().iterator().next());

        assertEquals(2, events.inactivatedGenes().size());
        InactivatedGene nf1 = findInactivatedGene(events.inactivatedGenes(), "NF1");
        assertFalse(nf1.hasBeenDeleted());
        InactivatedGene rb1 = findInactivatedGene(events.inactivatedGenes(), "RB1");
        assertTrue(rb1.hasBeenDeleted());

        assertEquals(1, events.amplifiedGenes().size());
        assertEquals("AMP", events.amplifiedGenes().iterator().next());

        assertEquals(1, events.wildtypeGenes().size());
        assertEquals("WILD", events.wildtypeGenes().iterator().next());

        assertEquals(1, events.fusions().size());
        FusionGene fusionGene = events.fusions().iterator().next();
        assertEquals("five", fusionGene.fiveGene());
        assertEquals("three", fusionGene.threeGene());
    }

    @NotNull
    private static InactivatedGene findInactivatedGene(@NotNull Iterable<InactivatedGene> inactivatedGenes, @NotNull String geneToFind) {
        for (InactivatedGene inactivatedGene : inactivatedGenes) {
            if (inactivatedGene.gene().equals(geneToFind)) {
                return inactivatedGene;
            }
        }

        throw new IllegalStateException("Could not find inactivated gene: " + geneToFind);
    }
}