package com.hartwig.actin.molecular.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import com.google.common.io.Resources;
import com.hartwig.actin.common.TestDataFactory;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;

import org.junit.Test;

public class MolecularRecordJsonTest {

    private static final String ORANGE_MOLECULAR_JSON = Resources.getResource("molecular/sample.orange.json").getPath();
    private static final String MINIMAL_MOLECULAR_JSON = Resources.getResource("molecular/sample.minimal.json").getPath();

    @Test
    public void canReadOrangeMolecularRecordJson() throws IOException {
        MolecularRecord record = MolecularRecordJson.read(ORANGE_MOLECULAR_JSON);

        assertEquals(TestDataFactory.TEST_SAMPLE, record.sampleId());
        assertTrue(record.hasReliableQuality());
        assertTrue(record.hasReliablePurity());
        assertEquals(25, record.genomicTreatmentEvidences().size());

        assertEquals(2, record.actionableGenomicEvents().size());
    }

    @Test
    public void canReadMinimalMolecularRecordJson() throws IOException {
        MolecularRecord record = MolecularRecordJson.read(MINIMAL_MOLECULAR_JSON);

        assertEquals(TestDataFactory.TEST_SAMPLE, record.sampleId());
        assertTrue(record.hasReliableQuality());
        assertTrue(record.hasReliablePurity());
        assertTrue(record.genomicTreatmentEvidences().isEmpty());
        assertTrue(record.actionableGenomicEvents().isEmpty());
    }
}