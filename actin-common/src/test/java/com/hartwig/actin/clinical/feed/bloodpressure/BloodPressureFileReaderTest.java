package com.hartwig.actin.clinical.feed.bloodpressure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.google.common.io.Resources;

import org.junit.Test;

public class BloodPressureFileReaderTest {

    private static final String TEST_BLOOD_PRESSURE_TSV = Resources.getResource("clinical/bloodpressure.tsv").getPath();

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canReadTestFile() throws IOException {
        List<BloodPressureEntry> entries = new BloodPressureFileReader().read(TEST_BLOOD_PRESSURE_TSV);
        assertEquals(1, entries.size());

        BloodPressureEntry entry = entries.get(0);

        assertEquals("ACTN-01-02-9999", entry.subject());
        assertEquals(LocalDate.of(2019, 4, 28), entry.effectiveDateTime());
        assertEquals("CS00000003", entry.codeCodeOriginal());
        assertEquals("NIBP", entry.codeDisplayOriginal());
        assertNull(entry.issued());
        assertTrue(entry.valueString().isEmpty());
        assertEquals("8481-6", entry.componentCodeCode());
        assertEquals("Systolic blood pressure", entry.componentCodeDisplay());
        assertEquals("mm[Hg]", entry.componentValueQuantityCode());
        assertEquals(108, entry.componentValueQuantityValue(), EPSILON);
    }
}