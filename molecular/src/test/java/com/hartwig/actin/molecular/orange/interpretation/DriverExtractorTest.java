package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableOrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class DriverExtractorTest {

    @Test
    public void removesDriversInCaseOfNoTumorCells() {
        OrangeRecord base = TestOrangeFactory.createProperTestOrangeRecord();
        OrangeRecord orange = ImmutableOrangeRecord.builder()
                .from(base)
                .purple(ImmutablePurpleRecord.builder()
                        .from(base.purple())
                        .fit(TestPurpleFactory.fitBuilder().from(base.purple().fit()).hasReliablePurity(false).build())
                        .build())
                .build();

        DriverExtractor driverExtractor = createTestExtractor();
        MolecularDrivers drivers = driverExtractor.extract(orange);

        assertTrue(drivers.variants().isEmpty());
        assertTrue(drivers.amplifications().isEmpty());
        assertTrue(drivers.losses().isEmpty());
        assertTrue(drivers.homozygousDisruptions().isEmpty());
        assertTrue(drivers.disruptions().isEmpty());
        assertTrue(drivers.fusions().isEmpty());
        assertTrue(drivers.viruses().isEmpty());
    }

    @Test
    public void canExtractFromProperTestData() {
        DriverExtractor driverExtractor = createTestExtractor();
        MolecularDrivers drivers = driverExtractor.extract(TestOrangeFactory.createProperTestOrangeRecord());

        assertEquals(1, drivers.variants().size());
        assertEquals(1, drivers.amplifications().size());
        assertEquals(1, drivers.losses().size());
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