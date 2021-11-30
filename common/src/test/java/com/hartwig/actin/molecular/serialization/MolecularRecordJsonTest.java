package com.hartwig.actin.molecular.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import com.google.common.io.Resources;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.molecular.datamodel.Gender;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;

import org.junit.Test;

public class MolecularRecordJsonTest {

    private static final String ORANGE_MOLECULAR_JSON = Resources.getResource("molecular/sample.orange.json").getPath();
    private static final String MINIMAL_MOLECULAR_JSON = Resources.getResource("molecular/sample.minimal.json").getPath();

    @Test
    public void canReadOrangeMolecularRecordJson() throws IOException {
        MolecularRecord record = MolecularRecordJson.read(ORANGE_MOLECULAR_JSON);

        assertEquals(TestDataFactory.TEST_SAMPLE, record.sampleId());
        assertEquals(Gender.MALE, record.gender());
        assertTrue(record.hasReliableQuality());

        assertEquals(1, record.configuredPrimaryTumorDoids().size());
        assertEquals(25, record.evidences().size());
    }

    @Test
    public void canReadMinimalMolecularRecordJson() throws IOException {
        MolecularRecord record = MolecularRecordJson.read(MINIMAL_MOLECULAR_JSON);

        assertEquals(TestDataFactory.TEST_SAMPLE, record.sampleId());
        assertNull(record.gender());
        assertTrue(record.hasReliableQuality());

        assertTrue(record.configuredPrimaryTumorDoids().isEmpty());
        assertTrue(record.evidences().isEmpty());
    }
}