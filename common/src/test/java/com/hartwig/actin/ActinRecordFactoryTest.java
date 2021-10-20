package com.hartwig.actin;

import static org.junit.Assert.assertNotNull;

import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;
import com.hartwig.actin.molecular.datamodel.TestMolecularDataFactory;

import org.junit.Test;

public class ActinRecordFactoryTest {

    @Test
    public void canCreateActinRecord() {
        assertNotNull(ActinRecordFactory.fromInputs(TestClinicalDataFactory.createMinimalTestClinicalRecord(),
                TestMolecularDataFactory.createMinimalTestMolecularRecord()));
    }
}