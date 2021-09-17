package com.hartwig.actin.clinical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import com.google.common.io.Resources;

import org.junit.Test;

public class ClinicalModelFactoryTest {

    private static final String CLINICAL_FEED_DIRECTORY = Resources.getResource("clinical/feed").getPath();
    private static final String TEST_SAMPLE = "ACTN01029999T";

    @Test
    public void canBuildClinicalModelFromTestDir() throws IOException {
        ClinicalModel model = ClinicalModelFactory.loadFromClinicalFeedDirectory(CLINICAL_FEED_DIRECTORY);

        ClinicalRecord record = model.findClinicalRecordForSample(TEST_SAMPLE);

        assertNotNull(record);
        assertEquals(TEST_SAMPLE, record.sampleId());

//        assertEquals(1953, record.patient().birthYear());
//        assertEquals(Sex.MALE, record.patient().sex());
//        assertEquals(LocalDate.parse("2020-07-13"), record.patient().registrationDate());

    }
}