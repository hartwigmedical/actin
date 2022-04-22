package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class MolecularDriverEntryComparatorTest {

    @Test
    public void canCompareMolecularDriverEntries() {
        MolecularDriverEntry entry1 = create(DriverLikelihood.MEDIUM, "driver 1");
        MolecularDriverEntry entry2 = create(DriverLikelihood.LOW, "driver 2");
        MolecularDriverEntry entry3 = create(DriverLikelihood.HIGH, "driver 3");
        MolecularDriverEntry entry4 = create(DriverLikelihood.HIGH, "driver 4");

        List<MolecularDriverEntry> entries = Lists.newArrayList(entry1, entry2, entry3, entry4);
        entries.sort(new MolecularDriverEntryComparator());

        assertEquals(entry3, entries.get(0));
        assertEquals(entry4, entries.get(1));
        assertEquals(entry1, entries.get(2));
        assertEquals(entry2, entries.get(3));
    }

    @NotNull
    private static MolecularDriverEntry create(@NotNull DriverLikelihood driverLikelihood, @NotNull String driver) {
        return ImmutableMolecularDriverEntry.builder().driverType(Strings.EMPTY).driver(driver).driverLikelihood(driverLikelihood).build();
    }
}