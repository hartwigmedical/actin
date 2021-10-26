package com.hartwig.actin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;
import com.hartwig.actin.molecular.datamodel.TestMolecularDataFactory;

import org.junit.Test;

public class PatientRecordFactoryTest {

    @Test
    public void canCreatePatientRecord() {
        assertNotNull(PatientRecordFactory.fromInputs(TestClinicalDataFactory.createMinimalTestClinicalRecord(),
                TestMolecularDataFactory.createMinimalTestMolecularRecord()));
    }

    @Test
    public void canConcatSets() {
        assertEquals("-", PatientRecordFactory.concat(Sets.newHashSet()));
        assertEquals("hi, there", PatientRecordFactory.concat(Sets.newHashSet("hi", "there")));
    }
}