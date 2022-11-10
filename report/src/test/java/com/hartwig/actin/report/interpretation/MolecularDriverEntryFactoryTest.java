package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;

import org.junit.Ignore;
import org.junit.Test;

public class MolecularDriverEntryFactoryTest {

    @Test
    @Ignore
    public void canCreateMolecularDriverEntries() {
        TreatmentMatch treatmentMatch = TestTreatmentMatchFactory.createMinimalTreatmentMatch();
        MolecularRecord record = TestMolecularFactory.createExhaustiveTestMolecularRecord();

        Set<MolecularDriverEntry> entries = MolecularDriverEntryFactory.create(treatmentMatch, record.drivers());

        assertEquals(7, entries.size());
    }
}