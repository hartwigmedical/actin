package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class MolecularDriverEntryComparatorTest {

    @Test
    public void canCompareMolecularDriverEntries() {
        MolecularDriverEntry entry1 = create(DriverLikelihood.MEDIUM, "driver type 1", "driver 1");
        MolecularDriverEntry entry2 = create(DriverLikelihood.LOW, "driver type 1", "driver 2");
        MolecularDriverEntry entry3 = create(DriverLikelihood.HIGH, "driver type 2", "driver 3");
        MolecularDriverEntry entry4 = create(DriverLikelihood.HIGH, "driver type 1", "driver 4");
        MolecularDriverEntry entry5 = create(DriverLikelihood.HIGH, "driver type 1", "driver 5");

        List<MolecularDriverEntry> entries = Lists.newArrayList(entry1, entry2, entry3, entry4, entry5);
        entries.sort(new MolecularDriverEntryComparator());

        assertEquals(entry4, entries.get(0));
        assertEquals(entry5, entries.get(1));
        assertEquals(entry3, entries.get(2));
        assertEquals(entry1, entries.get(3));
        assertEquals(entry2, entries.get(4));
    }

    @NotNull
    private static MolecularDriverEntry create(@NotNull DriverLikelihood driverLikelihood, @NotNull String driverType,
            @NotNull String driver) {
        return ImmutableMolecularDriverEntry.builder().driverType(driverType).driver(driver).driverLikelihood(driverLikelihood).build();
    }
}