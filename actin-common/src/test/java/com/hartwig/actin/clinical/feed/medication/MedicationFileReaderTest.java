package com.hartwig.actin.clinical.feed.medication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.google.common.io.Resources;

import org.junit.Test;

public class MedicationFileReaderTest {

    private static final String TEST_MEDICATION_TSV = Resources.getResource("clinical/feed/medication.tsv").getPath();

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canReadTestFile() throws IOException {
        List<MedicationEntry> entries = new MedicationFileReader().read(TEST_MEDICATION_TSV);
        assertEquals(1, entries.size());

        MedicationEntry entry = entries.get(0);

        assertEquals("ACTN-01-02-9999", entry.subject());
        assertEquals("P_90059949", entry.medicationReferenceMedicationValue());
        assertEquals("EPD", entry.medicationReferenceMedicationSystem());
        assertEquals("19-0716 PEMBROLIZUMAB V/P INFOPL 25MG/ML FL 4ML", entry.codeText());
        assertTrue(entry.code5ATCDisplay().isEmpty());
        assertTrue(entry.indicationDisplay().isEmpty());
        assertEquals("MILLIGRAM", entry.dosageInstructionDoseQuantityUnit());
        assertEquals(200, entry.dosageInstructionDoseQuantityValue(), EPSILON);
        assertTrue(entry.dosageInstructionFrequencyUnit().isEmpty());
        assertEquals(0, entry.dosageInstructionFrequencyValue(), EPSILON);
        assertEquals(0, entry.dosageInstructionMaxDosePerAdministration(), EPSILON);
        assertEquals("Excreta: nvt", entry.dosageInstructionPatientInstruction());
        assertTrue(entry.dosageInstructionAsNeededDisplay().isEmpty());
        assertTrue(entry.dosageInstructionPeriodBetweenDosagesUnit().isEmpty());
        assertEquals(0, entry.dosageInstructionPeriodBetweenDosagesValue(), EPSILON);
        assertEquals("200 milligram inlooptijd: 30 minuten, via 0,2 um filter", entry.dosageInstructionText());
        assertTrue(entry.status().isEmpty());
        assertTrue(entry.active().isEmpty());
        assertTrue(entry.dosageDoseValue().isEmpty());
        assertTrue(entry.dosageRateQuantityUnit().isEmpty());
        assertTrue(entry.dosageDoseUnitDisplayOriginal().isEmpty());
        assertEquals(LocalDate.of(2019, 6, 7), entry.periodOfUseValuePeriodStart());
        assertEquals(LocalDate.of(2019, 6, 7), entry.periodOfUseValuePeriodEnd());
        assertEquals("Definitief", entry.stopTypeDisplay());
        assertEquals("Inpatient", entry.categoryMedicationRequestCategoryDisplay());
        assertEquals("K", entry.categoryMedicationRequestCategoryCodeOriginal());
    }
}