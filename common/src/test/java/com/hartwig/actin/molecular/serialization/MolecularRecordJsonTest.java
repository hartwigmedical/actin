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

        assertEquals(3, molecular.actinTreatmentEvidence().size());
        MolecularEvidence actinEvidence1 = findByEvent(molecular.actinTreatmentEvidence(), "High TML");
        assertEquals("Trial 1", actinEvidence1.treatment());
        MolecularEvidence actinEvidence2 = findByEvent(molecular.actinTreatmentEvidence(), "HR deficiency");
        assertEquals("Trial 2", actinEvidence2.treatment());
        MolecularEvidence actinEvidence3 = findByEvent(molecular.actinTreatmentEvidence(), "NF1 disruption");
        assertEquals("Trial 3", actinEvidence3.treatment());

        assertEquals("trials", molecular.generalTrialSource());
        assertEquals(1, molecular.generalTrialEvidence().size());
        MolecularEvidence trialEvidence = findByEvent(molecular.generalTrialEvidence(), "High TML");
        assertEquals("Trial 1", trialEvidence.treatment());

        assertEquals("evidence", molecular.generalEvidenceSource());
        assertEquals(2, molecular.generalResponsiveEvidence().size());
        MolecularEvidence generalEvidence1 = findByTreatment(molecular.generalResponsiveEvidence(), "Pembrolizumab");
        assertEquals("High TML", generalEvidence1.event());
        MolecularEvidence generalEvidence2 = findByTreatment(molecular.generalResponsiveEvidence(), "Nivolumab");
        assertEquals("High TML", generalEvidence2.event());
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