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
        MolecularDriverEntry entry1 = create(DriverLikelihood.MEDIUM, "fusion", "driver 1");
        MolecularDriverEntry entry2 = create(DriverLikelihood.LOW, "disruption", "driver 2");
        MolecularDriverEntry entry3 = create(DriverLikelihood.HIGH, "mutation", "driver 3");
        MolecularDriverEntry entry4 = create(DriverLikelihood.HIGH, "amplification", "driver 4");
        MolecularDriverEntry entry5 = create(DriverLikelihood.HIGH, "amplification", "driver 5");
        MolecularDriverEntry entry6 = create(DriverLikelihood.HIGH, "virus", "driver 6");
        MolecularDriverEntry entry7 = create(DriverLikelihood.HIGH, "loss", "driver 7");
        MolecularDriverEntry entry8 = create(DriverLikelihood.HIGH, "disruption", "driver 7");
        MolecularDriverEntry entry9 = create(DriverLikelihood.HIGH, "fusion", "driver 7");


        List<MolecularDriverEntry> entries = Lists.newArrayList(entry1, entry2, entry3, entry4, entry5, entry6, entry7, entry8, entry9);
        entries.sort(new MolecularDriverEntryComparator());

        assertEquals(entry3, entries.get(0));
        assertEquals(entry4, entries.get(1));
        assertEquals(entry5, entries.get(2));
        assertEquals(entry7, entries.get(3));
        assertEquals(entry9, entries.get(4));
        assertEquals(entry8, entries.get(5));
        assertEquals(entry6, entries.get(6));
        assertEquals(entry1, entries.get(7));
        assertEquals(entry2, entries.get(8));
    }

    @NotNull
    private static MolecularDriverEntry create(@NotNull DriverLikelihood driverLikelihood, @NotNull String driverType,
            @NotNull String driver) {
        return ImmutableMolecularDriverEntry.builder().driverType(driverType).driver(driver).driverLikelihood(driverLikelihood).build();
    }
}