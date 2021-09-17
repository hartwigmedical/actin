package com.hartwig.actin.clinical.feed.lab;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.google.common.io.Resources;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class LabFileReaderTest {

    private static final String TEST_LAB_TSV = Resources.getResource("clinical/feed/lab.tsv").getPath();

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canReadTestFile() throws IOException {
        List<LabEntry> entries = new LabFileReader().read(TEST_LAB_TSV);
        assertEquals(2, entries.size());

        LabEntry entry1 = findByCodeCodeOriginal(entries, "HT");
        assertEquals("ACTN-01-02-9999", entry1.subject());
        assertEquals("Hematocriet", entry1.codeDisplayOriginal());
        assertEquals(LocalDate.of(2019, 6, 28), entry1.issued());
        assertEquals(Strings.EMPTY, entry1.valueQuantityComparator());
        assertEquals(0.36, entry1.valueQuantityValue(), EPSILON);
        assertEquals("L/L", entry1.valueQuantityUnit());
        assertEquals("Referentiewaarde \"te laag\" overschreden", entry1.interpretationDisplayOriginal());
        assertEquals(Strings.EMPTY, entry1.valueString());
        assertEquals(Strings.EMPTY, entry1.codeCode());
        assertEquals("0.42 - 0.52", entry1.referenceRangeText());

        LabEntry entry2 = findByCodeCodeOriginal(entries, "HB");
        assertEquals("ACTN-01-02-9999", entry2.subject());
        assertEquals("Hemoglobine", entry2.codeDisplayOriginal());
        assertEquals(LocalDate.of(2019, 5, 28), entry2.issued());
        assertEquals(Strings.EMPTY, entry2.valueQuantityComparator());
        assertEquals(4.2, entry2.valueQuantityValue(), EPSILON);
        assertEquals("mmol/L", entry2.valueQuantityUnit());
        assertEquals("Referentiewaarde \"te laag\" overschreden", entry2.interpretationDisplayOriginal());
        assertEquals(Strings.EMPTY, entry2.valueString());
        assertEquals(Strings.EMPTY, entry2.codeCode());
        assertEquals("8.8 - 10.7", entry2.referenceRangeText());
    }

    @NotNull
    private static LabEntry findByCodeCodeOriginal(@NotNull List<LabEntry> entries, @NotNull String codeCodeOriginal) {
        for (LabEntry entry : entries) {
            if (entry.codeCodeOriginal().equals(codeCodeOriginal)) {
                return entry;
            }
        }

        throw new IllegalStateException("No lab entry found with codeCodeOriginal '" + codeCodeOriginal + "'");
    }
}