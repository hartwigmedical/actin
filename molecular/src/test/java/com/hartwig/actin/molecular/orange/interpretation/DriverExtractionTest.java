package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertNotNull;

import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeDataFactory;

import org.junit.Test;

public class DriverExtractionTest {

    @Test
    public void canExtractForTestData() {
        MolecularDrivers drivers = DriverExtraction.extract(TestOrangeDataFactory.createProperTestOrangeRecord());

        assertNotNull(drivers);
    }
}