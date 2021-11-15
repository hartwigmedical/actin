package com.hartwig.actin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;
import com.hartwig.actin.molecular.datamodel.TestMolecularDataFactory;

import org.junit.Test;

public class PatientRecordFactoryTest {

    @Test
    public void canCreatePatientRecordFromTestRecords() {
        assertNotNull(PatientRecordFactory.fromInputs(TestClinicalDataFactory.createMinimalTestClinicalRecord(),
                TestMolecularDataFactory.createMinimalTestMolecularRecord()));

        assertNotNull(PatientRecordFactory.fromInputs(TestClinicalDataFactory.createProperTestClinicalRecord(),
                TestMolecularDataFactory.createProperTestMolecularRecord()));
    }

    @Test
    public void doNotCrashOnMissingTumorDoids() {
        ClinicalRecord base = TestClinicalDataFactory.createMinimalTestClinicalRecord();

        ClinicalRecord noTumorDoid = ImmutableClinicalRecord.builder()
                .from(base)
                .tumor(ImmutableTumorDetails.builder().from(base.tumor()).doids(null).build())
                .build();

        assertNotNull(PatientRecordFactory.fromInputs(noTumorDoid, TestMolecularDataFactory.createMinimalTestMolecularRecord()));
    }

    @Test
    public void canConcatSets() {
        assertEquals("-", PatientRecordFactory.concat(Sets.newHashSet()));
        assertEquals("hi, there", PatientRecordFactory.concat(Sets.newHashSet("hi", "there")));
    }
}