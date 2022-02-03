package com.hartwig.actin.molecular.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import com.google.common.io.Resources;
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.FusionGene;
import com.hartwig.actin.molecular.datamodel.GeneMutation;
import com.hartwig.actin.molecular.datamodel.InactivatedGene;
import com.hartwig.actin.molecular.datamodel.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
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
        assertTrue(molecular.hasReliableQuality());

        assertEquals(1, molecular.mutations().size());
        GeneMutation geneMutation = molecular.mutations().iterator().next();
        assertEquals("TP53", geneMutation.gene());
        assertEquals("exon 1", geneMutation.mutation());

        assertEquals(1, molecular.activatedGenes().size());
        assertEquals("ACT", molecular.activatedGenes().iterator().next());

        assertEquals(2, molecular.inactivatedGenes().size());
        InactivatedGene nf1 = findInactivatedGene(molecular.inactivatedGenes(), "NF1");
        assertFalse(nf1.hasBeenDeleted());
        InactivatedGene rb1 = findInactivatedGene(molecular.inactivatedGenes(), "RB1");
        assertTrue(rb1.hasBeenDeleted());

        assertEquals(1, molecular.amplifiedGenes().size());
        assertEquals("AMP", molecular.amplifiedGenes().iterator().next());

        assertEquals(1, molecular.wildtypeGenes().size());
        assertEquals("WILD", molecular.wildtypeGenes().iterator().next());

        assertEquals(1, molecular.fusions().size());
        FusionGene fusionGene = molecular.fusions().iterator().next();
        assertEquals("five", fusionGene.fiveGene());
        assertEquals("three", fusionGene.threeGene());

        assertFalse(molecular.isMicrosatelliteUnstable());
        assertTrue(molecular.isHomologousRepairDeficient());
        assertEquals(4.32, molecular.tumorMutationalBurden(), EPSILON);
        assertEquals(243, (int) molecular.tumorMutationalLoad());

        assertEquals(3, molecular.actinTrials().size());
        MolecularEvidence actinTrial1 = findByEvent(molecular.actinTrials(), "High TML");
        assertEquals("Trial 1", actinTrial1.treatment());
        MolecularEvidence actinTrial2 = findByEvent(molecular.actinTrials(), "HR deficiency");
        assertEquals("Trial 2", actinTrial2.treatment());
        MolecularEvidence actinTrial3 = findByEvent(molecular.actinTrials(), "NF1 disruption");
        assertEquals("Trial 3", actinTrial3.treatment());

        assertEquals("trials", molecular.externalTrialSource());
        assertEquals(1, molecular.externalTrials().size());
        MolecularEvidence externalTrial = findByEvent(molecular.externalTrials(), "High TML");
        assertEquals("Trial 1", externalTrial.treatment());

        assertEquals("evidence", molecular.evidenceSource());
        assertEquals(2, molecular.approvedResponsiveEvidence().size());
        MolecularEvidence approvedEvidence1 = findByTreatment(molecular.approvedResponsiveEvidence(), "Pembrolizumab");
        assertEquals("High TML", approvedEvidence1.event());
        MolecularEvidence approvedEvidence2 = findByTreatment(molecular.approvedResponsiveEvidence(), "Nivolumab");
        assertEquals("High TML", approvedEvidence2.event());

        assertTrue(molecular.experimentalResponsiveEvidence().isEmpty());
        assertTrue(molecular.otherResponsiveEvidence().isEmpty());
        assertTrue(molecular.resistanceEvidence().isEmpty());
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

    @NotNull
    private static MolecularEvidence findByEvent(@NotNull Iterable<MolecularEvidence> evidences, @NotNull String eventToFind) {
        for (MolecularEvidence evidence : evidences) {
            if (evidence.event().equals(eventToFind)) {
                return evidence;
            }
        }

        throw new IllegalStateException("Could not find evidence with event: " + eventToFind);
    }

    @NotNull
    private static MolecularEvidence findByTreatment(@NotNull Iterable<MolecularEvidence> evidences, @NotNull String treatmentToFind) {
        for (MolecularEvidence evidence : evidences) {
            if (evidence.treatment().equals(treatmentToFind)) {
                return evidence;
            }
        }

        throw new IllegalStateException("Could not find evidence with treatment: " + treatmentToFind);
    }
}