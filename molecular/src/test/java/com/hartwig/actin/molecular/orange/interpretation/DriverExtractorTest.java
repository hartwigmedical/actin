package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class DriverExtractorTest {

    @Test
    public void canExtractFromProperTestData() {
        DriverExtractor driverExtractor = createTestExtractor();
        MolecularDrivers drivers = driverExtractor.extract(TestOrangeFactory.createProperTestOrangeRecord());

        assertEquals(1, drivers.variants().size());
        assertEquals(2, drivers.copyNumbers().size());
        assertEquals(1, drivers.homozygousDisruptions().size());
        assertEquals(1, drivers.disruptions().size());
        assertEquals(1, drivers.fusions().size());
        assertEquals(1, drivers.viruses().size());
    }

    @NotNull
    private static DriverExtractor createTestExtractor() {
        return DriverExtractor.create(TestGeneFilterFactory.createAlwaysValid(), TestEvidenceDatabaseFactory.createEmptyDatabase());
    }
}