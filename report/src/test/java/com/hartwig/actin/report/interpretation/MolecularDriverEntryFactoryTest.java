package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;

import org.junit.Test;

public class MolecularDriverEntryFactoryTest {

    @Test
    public void canCreateMolecularDriverEntries() {
        MolecularRecord record = TestMolecularFactory.createExhaustiveTestMolecularRecord();

        MolecularDriverEntryFactory factory = new MolecularDriverEntryFactory(ArrayListMultimap.create());
        Set<MolecularDriverEntry> entries = factory.create(record);

        assertEquals(7, entries.size());
    }
}