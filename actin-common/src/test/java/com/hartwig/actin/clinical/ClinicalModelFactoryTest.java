package com.hartwig.actin.clinical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.google.common.io.Resources;
import com.hartwig.actin.clinical.curation.TestCurationFactory;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.Sex;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;
import com.hartwig.actin.clinical.feed.TestFeedFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ClinicalModelFactoryTest {

    private static final String FEED_DIRECTORY = Resources.getResource("clinical/feed").getPath();
    private static final String CURATION_DIRECTORY = Resources.getResource("clinical/curation").getPath();

    private static final String TEST_SAMPLE = "ACTN01029999T";

    @Test
    public void canCreateFromFeedAndCurationDirectories() throws IOException {
        assertNotNull(ClinicalModelFactory.fromFeedAndCurationDirectories(FEED_DIRECTORY, CURATION_DIRECTORY));
    }

    @Test
    public void canCreateClinicalModelFromMinimalTestData() {
        ClinicalModel model = createMinimalTestClinicalModel();
        assertEquals(1, model.records().size());

        ClinicalRecord record = model.findClinicalRecordForSample(TEST_SAMPLE);
        assertNotNull(record);
        assertEquals(TEST_SAMPLE, record.sampleId());
    }

    @Test
    public void canCreateClinicalModelFromProperTestData() {
        ClinicalModel model = createProperTestClinicalModel();

        assertEquals(1, model.records().size());
        ClinicalRecord record = model.findClinicalRecordForSample(TEST_SAMPLE);

        assertNotNull(record);
        assertEquals(TEST_SAMPLE, record.sampleId());

        assertPatientDetails(record.patient());
        assertToxicities(record.toxicities());
    }

    private static void assertPatientDetails(@NotNull PatientDetails patient) {
        assertEquals(1960, patient.birthYear());
        assertEquals(Sex.MALE, patient.sex());
        assertEquals(LocalDate.of(2021, 6, 1), patient.registrationDate());
        assertEquals(LocalDate.of(2021, 8, 1), patient.questionnaireDate());
    }

    private static void assertToxicities(@NotNull List<Toxicity> toxicities) {
        assertEquals(1, toxicities.size());

        Toxicity toxicity = findByName(toxicities, "Nausea");
        assertEquals(ToxicitySource.EHR, toxicity.source());
        assertEquals(2, (int) toxicity.grade());
    }

    @NotNull
    private static Toxicity findByName(@NotNull List<Toxicity> toxicities, @NotNull String name) {
        for (Toxicity toxicity : toxicities) {
            if (toxicity.name().equals(name)) {
                return toxicity;
            }
        }

        throw new IllegalStateException("Could not find toxicity with name: " + name);
    }

    @NotNull
    private static ClinicalModel createMinimalTestClinicalModel() {
        return new ClinicalModelFactory(TestFeedFactory.createMinimalTestFeedModel(),
                TestCurationFactory.createMinimalTestCurationModel()).create();
    }

    @NotNull
    private static ClinicalModel createProperTestClinicalModel() {
        return new ClinicalModelFactory(TestFeedFactory.createProperTestFeedModel(),
                TestCurationFactory.createProperTestCurationModel()).create();
    }
}