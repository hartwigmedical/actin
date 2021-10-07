package com.hartwig.actin.datamodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import com.google.common.io.Resources;
import com.hartwig.actin.datamodel.clinical.ClinicalRecord;

import org.junit.Test;

public class ClinicalModelFileTest {

    private static final String EMPTY_CLINICAL_MODEL_JSON = Resources.getResource("clinical/clinical_model_empty.json").getPath();
    private static final String FULL_CLINICAL_MODEL_JSON = Resources.getResource("clinical/clinical_model_example.json").getPath();

    @Test
    public void canConvertBackAndForthJson() throws IOException {
        // TODO Define model in-memory
        ClinicalModel model = ClinicalModelFile.read(FULL_CLINICAL_MODEL_JSON);

        String json = ClinicalModelFile.toJson(model);
        ClinicalModel convertedModel = ClinicalModelFile.fromJson(json);

        assertEquals(model.records().get(0), convertedModel.records().get(0));
    }

    @Test
    public void canReadFullClinicalModelFile() throws IOException {
        ClinicalModel model = ClinicalModelFile.read(FULL_CLINICAL_MODEL_JSON);
        assertEquals(1, model.records().size());

        ClinicalRecord record = model.findClinicalRecordForSample("ACTN01029999T");
        assertNotNull(record);

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

    @Test
    public void canReadEmptyClinicalModelFile() throws IOException {
        assertTrue(ClinicalModelFile.read(EMPTY_CLINICAL_MODEL_JSON).records().isEmpty());
    }
}