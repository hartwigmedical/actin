package com.hartwig.actin.clinical.serialization;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.io.Resources;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ClinicalRecordJsonTest {

    private static final String CLINICAL_DIRECTORY = Resources.getResource("clinical").getPath();
    private static final String CLINICAL_JSON = CLINICAL_DIRECTORY + File.separator + "sample.clinical.json";

    @Test
    public void canConvertBackAndForthJson() {
        ClinicalRecord minimal = TestClinicalDataFactory.createMinimalTestClinicalRecord();
        ClinicalRecord convertedMinimal = ClinicalRecordJson.fromJson(ClinicalRecordJson.toJson(minimal));

        assertEquals(minimal, convertedMinimal);

        ClinicalRecord proper = TestClinicalDataFactory.createProperTestClinicalRecord();
        ClinicalRecord convertedProper = ClinicalRecordJson.fromJson(ClinicalRecordJson.toJson(proper));

        assertEquals(proper, convertedProper);
    }

    @Test
    public void canReadClinicalRecordDirectory() throws IOException {
        List<ClinicalRecord> records = ClinicalRecordJson.readFromDir(CLINICAL_DIRECTORY);
        assertEquals(1, records.size());

        assertClinicalRecord(records.get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotReadFilesFromNonDir() throws IOException {
        ClinicalRecordJson.readFromDir(CLINICAL_JSON);
    }

    @Test
    public void canReadClinicalRecordJson() throws IOException {
        assertClinicalRecord(ClinicalRecordJson.read(CLINICAL_JSON));
    }

    private static void assertClinicalRecord(@NotNull ClinicalRecord record) {
        assertEquals("ACTN01029999T", record.sampleId());
        assertEquals(1, record.priorTumorTreatments().size());
        assertEquals(1, record.priorSecondPrimaries().size());
        assertEquals(1, record.priorOtherConditions().size());
        assertEquals(1, record.cancerRelatedComplications().size());
        assertEquals(2, record.labValues().size());
        assertEquals(2, record.toxicities().size());
        assertEquals(2, record.allergies().size());
        assertEquals(1, record.surgeries().size());
        assertEquals(1, record.vitalFunctions().size());
        assertEquals(1, record.bloodTransfusions().size());
        assertEquals(2, record.medications().size());
    }
}