package com.hartwig.actin.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import com.google.common.io.Resources;
import com.hartwig.actin.datamodel.TestDataFactory;
import com.hartwig.actin.datamodel.molecular.MolecularRecord;

import org.junit.Test;

public class MolecularRecordJsonTest {

    private static final String MOLECULAR_JSON = Resources.getResource("molecular/sample.orange.json").getPath();

    @Test
    public void canReadMolecularRecordJson() throws IOException {
        MolecularRecord record = MolecularRecordJson.read(MOLECULAR_JSON);

        assertEquals(TestDataFactory.TEST_SAMPLE, record.sampleId());
        assertTrue(record.hasReliableQuality());
        assertTrue(record.hasReliablePurity());
        assertEquals(25, record.genomicTreatmentEvidences().size());
    }
}