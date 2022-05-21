package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;

import org.junit.Test;

public class MolecularDriverEntryFactoryTest {

    @Test
    public void canCreateMolecularDriverEntries() {
        MolecularRecord record = TestMolecularFactory.createExhaustiveTestMolecularRecord();

        Set<MolecularDriverEntry> entries = MolecularDriverEntryFactory.create(record.drivers(), record.evidence());

        assertEquals(7, entries.size());
    }
}