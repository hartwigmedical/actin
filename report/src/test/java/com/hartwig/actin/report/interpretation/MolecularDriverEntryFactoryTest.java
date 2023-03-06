package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;
import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class MolecularDriverEntryFactoryTest {

    @Test
    public void canCreateMolecularDriverEntries() {
        MolecularRecord record = TestMolecularFactory.createExhaustiveTestMolecularRecord();

        MolecularDriverEntryFactory factory = MolecularDriverEntryFactory.fromEvaluatedTrials(Lists.newArrayList());
        Set<MolecularDriverEntry> entries = factory.create(record);

        assertEquals(7, entries.size());
    }

    @Test
    public void canMatchActinTrialToMolecularDrivers() {
        MolecularRecord record = TestMolecularFactory.createProperTestMolecularRecord();
        assertFalse(record.drivers().variants().isEmpty());

        Variant firstVariant = record.drivers().variants().iterator().next();
        EvaluatedCohort openTrialForVariant = EvaluatedCohortTestFactory.builder()
                .acronym("trial 1")
                .addMolecularEvents(firstVariant.event())
                .isPotentiallyEligible(true)
                .isOpen(true)
                .build();

        EvaluatedCohort closedTrialForVariant = EvaluatedCohortTestFactory.builder()
                .acronym("trial 2")
                .addMolecularEvents(firstVariant.event())
                .isPotentiallyEligible(true)
                .isOpen(false)
                .build();

        MolecularDriverEntryFactory factory =
                MolecularDriverEntryFactory.fromEvaluatedTrials(Lists.newArrayList(openTrialForVariant, closedTrialForVariant));
        Set<MolecularDriverEntry> entries = factory.create(record);

        MolecularDriverEntry entry = startsWithDriver(entries, firstVariant.event());
        assertEquals(1, entry.actinTrials().size());
        assertTrue(entry.actinTrials().contains("trial 1"));
    }

    @NotNull
    private static MolecularDriverEntry startsWithDriver(@NotNull Set<MolecularDriverEntry> entries, @NotNull String driverToFind) {
        for (MolecularDriverEntry entry : entries) {
            if (entry.driver().startsWith(driverToFind)) {
                return entry;
            }
        }

        throw new IllegalStateException("Could not find molecular driver entry starting with driver: " + driverToFind);
    }
}