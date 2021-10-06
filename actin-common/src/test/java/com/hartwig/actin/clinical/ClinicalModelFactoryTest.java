package com.hartwig.actin.clinical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.google.common.io.Resources;
import com.hartwig.actin.clinical.curation.TestCurationFactory;
import com.hartwig.actin.clinical.datamodel.Allergy;
import com.hartwig.actin.clinical.datamodel.BloodPressure;
import com.hartwig.actin.clinical.datamodel.ClinicalStatus;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.Sex;
import com.hartwig.actin.clinical.datamodel.Surgery;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.clinical.feed.TestFeedFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ClinicalModelFactoryTest {

    private static final String FEED_DIRECTORY = Resources.getResource("clinical/feed").getPath();
    private static final String CURATION_DIRECTORY = Resources.getResource("clinical/curation").getPath();

    private static final String TEST_SAMPLE = "ACTN01029999T";

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canCreateFromFeedAndCurationDirectories() throws IOException {
        assertNotNull(ClinicalModelFactory.fromFeedAndCurationDirectories(FEED_DIRECTORY, CURATION_DIRECTORY));
    }

    @Test
    public void canCreateClinicalModelFromMinimalTestData() {
        ClinicalModel model = createMinimalTestClinicalModel();

        assertEquals(1, model.records().size());
        assertNotNull(model.findClinicalRecordForSample(TEST_SAMPLE));
    }

    @Test
    public void canCreateClinicalModelFromProperTestData() {
        ClinicalModel model = createProperTestClinicalModel();

        assertEquals(1, model.records().size());
        ClinicalRecord record = model.findClinicalRecordForSample(TEST_SAMPLE);

        assertNotNull(record);
        assertPatientDetails(record.patient());
        assertTumorDetails(record.tumor());
        assertClinicalStatus(record.clinicalStatus());
        assertToxicities(record.toxicities());
        assertAllergies(record.allergies());
        assertSurgeries(record.surgeries());
        assertBloodPressures(record.bloodPressures());
    }

    private static void assertPatientDetails(@NotNull PatientDetails patient) {
        assertEquals(1960, patient.birthYear());
        assertEquals(Sex.MALE, patient.sex());
        assertEquals(LocalDate.of(2021, 6, 1), patient.registrationDate());
        assertEquals(LocalDate.of(2021, 8, 1), patient.questionnaireDate());
    }

    private static void assertTumorDetails(@NotNull TumorDetails tumor) {
        assertNull(tumor.primaryTumorLocation());
        assertNull(tumor.primaryTumorSubLocation());
        assertNull(tumor.primaryTumorType());
        assertNull(tumor.primaryTumorSubType());
        assertNull(tumor.primaryTumorExtraDetails());
        assertNull(tumor.doids());
        assertEquals(TumorStage.III, tumor.stage());
        assertTrue(tumor.hasMeasurableLesionRecist());
        assertNull(tumor.hasBrainLesions());
        assertNull(tumor.hasActiveBrainLesions());
        assertNull(tumor.hasSymptomaticBrainLesions());
        assertNull(tumor.hasCnsLesions());
        assertNull(tumor.hasActiveCnsLesions());
        assertNull(tumor.hasSymptomaticCnsLesions());
        assertFalse(tumor.hasBoneLesions());
        assertFalse(tumor.hasLiverLesions());
        assertTrue(tumor.hasOtherLesions());
        assertTrue(tumor.otherLesions().contains("Pulmonal"));
        assertEquals("Lymph node", tumor.biopsyLocation());
    }

    private static void assertClinicalStatus(@NotNull ClinicalStatus clinicalStatus) {
        assertEquals(0, (int) clinicalStatus.who());
        assertFalse(clinicalStatus.hasActiveInfection());
        assertFalse(clinicalStatus.hasSigAberrationLatestEcg());
        assertTrue(clinicalStatus.ecgAberrationDescription().isEmpty());
    }

    private static void assertToxicities(@NotNull List<Toxicity> toxicities) {
        assertEquals(1, toxicities.size());

        Toxicity toxicity = toxicities.get(0);
        assertEquals("Nausea", toxicity.name());
        assertEquals(ToxicitySource.EHR, toxicity.source());
        assertEquals(2, (int) toxicity.grade());
    }

    private static void assertAllergies(@NotNull List<Allergy> allergies) {
        assertEquals(1, allergies.size());

        Allergy allergy = allergies.get(0);
        assertEquals("pills", allergy.name());
        assertEquals("medication", allergy.category());
        assertEquals("unknown", allergy.criticality());
    }

    private static void assertSurgeries(@NotNull List<Surgery> surgeries) {
        assertEquals(1, surgeries.size());

        Surgery surgery = surgeries.get(0);
        assertEquals(LocalDate.of(2015, 10, 10), surgery.endDate());
    }

    private static void assertBloodPressures(@NotNull List<BloodPressure> bloodPressures) {
        assertEquals(1, bloodPressures.size());

        BloodPressure bloodPressure = bloodPressures.get(0);
        assertEquals(LocalDate.of(2021, 2, 27), bloodPressure.date());
        assertEquals("systolic", bloodPressure.category());
        assertEquals(120, bloodPressure.value(), EPSILON);
        assertEquals("mm[Hg]", bloodPressure.unit());
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