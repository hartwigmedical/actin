package com.hartwig.actin.datamodel;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import com.google.common.io.Resources;
import com.hartwig.actin.datamodel.clinical.ClinicalRecord;

import org.junit.Test;

public class ClinicalRecordFileTest {

    private static final String CLINICAL_DIRECTORY = Resources.getResource("clinical").getPath();

    @Test
    public void canConvertBackAndForthJson() throws IOException {
        // TODO Define model in-memory
        List<ClinicalRecord> records = ClinicalRecordFile.read(CLINICAL_DIRECTORY);

        ClinicalRecord original = records.get(0);
        String json = ClinicalRecordFile.toJson(original);
        ClinicalRecord convertedRecord = ClinicalRecordFile.fromJson(json);

        assertEquals(original, convertedRecord);
    }

    @Test
    public void canReadClinicalRecordDirectory() throws IOException {
        List<ClinicalRecord> records = ClinicalRecordFile.read(CLINICAL_DIRECTORY);
        assertEquals(1, records.size());

        ClinicalRecord record = records.get(0);

        assertEquals("ACTN01029999T", record.sampleId());
        assertEquals(1, record.priorTumorTreatments().size());
        assertEquals(1, record.priorSecondPrimaries().size());
        assertEquals(1, record.priorOtherConditions().size());
        assertEquals(1, record.cancerRelatedComplications().size());
        assertEquals(1, record.otherComplications().size());
        assertEquals(2, record.labValues().size());
        assertEquals(2, record.toxicities().size());
        assertEquals(2, record.allergies().size());
        assertEquals(1, record.surgeries().size());
        assertEquals(1, record.bloodPressures().size());
        assertEquals(1, record.bloodTransfusions().size());
        assertEquals(2, record.medications().size());
    }
}