package com.hartwig.actin.molecular.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import com.google.common.io.Resources;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;

import org.junit.Test;

public class MolecularRecordJsonTest {

    private static final String ORANGE_MOLECULAR_JSON = Resources.getResource("molecular/sample.orange.json").getPath();
    private static final String MINIMAL_MOLECULAR_JSON = Resources.getResource("molecular/sample.minimal.json").getPath();

    private static final double EPSILON = 1.0E-2;

    @Test
    public void canReadOrangeMolecularRecordJson() throws IOException {
        MolecularRecord record = MolecularRecordJson.read(ORANGE_MOLECULAR_JSON);

        assertEquals(TestDataFactory.TEST_SAMPLE, record.sampleId());
        assertTrue(record.hasReliableQuality());

        assertEquals(1, record.configuredPrimaryTumorDoids().size());
        assertFalse(record.isMicrosatelliteUnstable());
        assertFalse(record.isHomologousRepairDeficient());
        assertEquals(13.71, record.tumorMutationalBurden(), EPSILON);
        assertEquals(189, record.tumorMutationalLoad());
        assertEquals(25, record.evidences().size());
    }

    @Test
    public void canReadMinimalMolecularRecordJson() throws IOException {
        MolecularRecord record = MolecularRecordJson.read(MINIMAL_MOLECULAR_JSON);

        assertEquals(TestDataFactory.TEST_SAMPLE, record.sampleId());
        assertFalse(record.hasReliableQuality());

        assertTrue(record.configuredPrimaryTumorDoids().isEmpty());
        assertFalse(record.isMicrosatelliteUnstable());
        assertFalse(record.isHomologousRepairDeficient());

        assertTrue(record.evidences().isEmpty());
    }
}