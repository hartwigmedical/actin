package com.hartwig.actin.molecular.orange.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import com.google.common.io.Resources;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;

import org.junit.Test;

public class OrangeJsonTest {

    private static final String PROPER_ORANGE_JSON = Resources.getResource("orange/proper.orange.json").getPath();
    private static final String MINIMAL_ORANGE_JSON = Resources.getResource("orange/minimal.orange.json").getPath();

    private static final double EPSILON = 1.0E-2;

    @Test
    public void canReadProperOrangeRecordJson() throws IOException {
        OrangeRecord record = OrangeJson.read(PROPER_ORANGE_JSON);

        assertEquals(TestDataFactory.TEST_SAMPLE, record.sampleId());
        assertTrue(record.hasReliableQuality());

        assertEquals("MSS", record.microsatelliteStabilityStatus());
        assertEquals("HR_PROFICIENT", record.homologousRepairStatus());
        assertEquals(13.71, record.tumorMutationalBurden(), EPSILON);
        assertEquals(185, record.tumorMutationalLoad());
        assertEquals(138, record.evidences().size());
    }

    @Test
    public void canReadMinimalOrangeRecordJson() throws IOException {
        OrangeRecord record = OrangeJson.read(MINIMAL_ORANGE_JSON);

        assertEquals(TestDataFactory.TEST_SAMPLE, record.sampleId());
        assertFalse(record.hasReliableQuality());

        assertEquals("MSS", record.microsatelliteStabilityStatus());
        assertEquals("HR_PROFICIENT", record.homologousRepairStatus());

        assertTrue(record.evidences().isEmpty());
    }
}