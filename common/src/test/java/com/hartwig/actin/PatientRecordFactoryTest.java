package com.hartwig.actin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;

import org.junit.Test;

public class PatientRecordFactoryTest {

    @Test
    public void canCreatePatientRecordFromTestRecords() {
        assertNotNull(PatientRecordFactory.fromInputs(TestClinicalFactory.createMinimalTestClinicalRecord(),
                TestMolecularFactory.createMinimalTestMolecularRecord()));

        assertNotNull(PatientRecordFactory.fromInputs(TestClinicalFactory.createProperTestClinicalRecord(),
                TestMolecularFactory.createProperTestMolecularRecord()));
    }

    @Test
    public void doNotCrashOnMissingTumorDoids() {
        ClinicalRecord base = TestClinicalFactory.createMinimalTestClinicalRecord();

        ClinicalRecord noTumorDoid = ImmutableClinicalRecord.builder()
                .from(base)
                .tumor(ImmutableTumorDetails.builder().from(base.tumor()).doids(null).build())
                .build();

        assertNotNull(PatientRecordFactory.fromInputs(noTumorDoid, TestMolecularFactory.createMinimalTestMolecularRecord()));
    }

    @Test
    public void clinicalSampleBeatsMolecularSample() {
        ClinicalRecord clinical = ImmutableClinicalRecord.builder()
                .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                .sampleId("clinical")
                .build();

        MolecularRecord molecular = ImmutableMolecularRecord.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord())
                .sampleId("molecular")
                .build();

        PatientRecord patient = PatientRecordFactory.fromInputs(clinical, molecular);
        assertEquals("clinical", patient.sampleId());
    }
}