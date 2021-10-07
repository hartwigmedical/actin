package com.hartwig.actin.datamodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import com.google.common.io.Resources;

import org.junit.Test;

public class ClinicalModelFileTest {

    private static final String EMPTY_CLINICAL_MODEL_JSON = Resources.getResource("clinical/clinical_model_empty.json").getPath();
    private static final String FULL_CLINICAL_MODEL_JSON = Resources.getResource("clinical/clinical_model_example.json").getPath();

    @Test
    public void canReadFullClinicalModelFile() throws IOException {
        ClinicalModel model = ClinicalModelFile.read(FULL_CLINICAL_MODEL_JSON);
        assertEquals(1, model.records().size());
    }

    @Test
    public void canReadEmptyClinicalModelFile() throws IOException {
        assertTrue(ClinicalModelFile.read(EMPTY_CLINICAL_MODEL_JSON).records().isEmpty());
    }
}