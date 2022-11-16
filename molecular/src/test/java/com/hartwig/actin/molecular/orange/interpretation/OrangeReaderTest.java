package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class OrangeReaderTest {

    @Test
    public void canReadOrangeRecord() {
        MolecularRecord record = createTestReader().read(TestOrangeFactory.createProperTestOrangeRecord());

        assertEquals(TestDataFactory.TEST_PATIENT, record.patientId());
        assertEquals(TestDataFactory.TEST_SAMPLE, record.sampleId());
        assertEquals(ExperimentType.WGS, record.type());
        assertEquals(LocalDate.of(2021, 5, 6), record.date());
        assertTrue(record.hasSufficientQuality());

        assertNotNull(record.characteristics());

        MolecularDrivers drivers = record.drivers();
        assertEquals(1, drivers.variants().size());
        assertEquals(1, drivers.amplifications().size());
        assertEquals(1, drivers.losses().size());
        assertEquals(1, drivers.homozygousDisruptions().size());
        assertEquals(1, drivers.disruptions().size());
        assertEquals(1, drivers.fusions().size());
        assertEquals(1, drivers.viruses().size());

        assertEquals(1, record.pharmaco().size());
    }

    @Test
    public void canConvertSampleIdToPatientId() {
        assertEquals("ACTN01029999", OrangeReader.toPatientId("ACTN01029999T"));
        assertEquals("ACTN01029999", OrangeReader.toPatientId("ACTN01029999T2"));
    }

    @Test (expected = IllegalArgumentException.class)
    public void crashOnInvalidSampleId() {
        OrangeReader.toPatientId("no sample");
    }

    @NotNull
    private static OrangeReader createTestReader() {
        return new OrangeReader(TestGeneFilterFactory.createNeverValid());
    }
}