package com.hartwig.actin.datamodel;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import com.google.common.io.Resources;

import org.junit.Test;

public class ClinicalModelFileTest {

    private static final String CLINICAL_MODEL_JSON = Resources.getResource("clinical/clinical_model_example.json").getPath();

    @Test
    public void canReadClinicalModelFile() throws IOException {
        assertNotNull(ClinicalModelFile.read(CLINICAL_MODEL_JSON));
    }
}