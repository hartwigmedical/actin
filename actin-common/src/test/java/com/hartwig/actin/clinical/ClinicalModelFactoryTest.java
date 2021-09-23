package com.hartwig.actin.clinical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;

import com.hartwig.actin.clinical.curation.TestCurationFactory;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.Sex;
import com.hartwig.actin.clinical.feed.TestFeedFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ClinicalModelFactoryTest {

    private static final String TEST_SAMPLE = "ACTN01029999T";

    @Test
    public void canCreateClinicalModelFromTestData() {
        ClinicalModel model = createTestClinicalModel();

        assertEquals(1, model.records().size());
        ClinicalRecord record = model.findClinicalRecordForSample(TEST_SAMPLE);

        assertNotNull(record);
        assertEquals(TEST_SAMPLE, record.sampleId());

        assertPatientDetails(record.patient());
    }

    private static void assertPatientDetails(@NotNull PatientDetails patient) {
        assertEquals(1960, patient.birthYear());
        assertEquals(Sex.MALE, patient.sex());
        assertEquals(LocalDate.of(2021, 6, 1), patient.registrationDate());
        assertEquals(LocalDate.of(2021, 8, 1), patient.questionnaireDate());
    }

    @NotNull
    private static ClinicalModel createTestClinicalModel() {
        return new ClinicalModelFactory(TestFeedFactory.createTestFeedModel(), TestCurationFactory.createTestCurationModel()).create();
    }
}