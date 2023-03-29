package com.hartwig.actin.clinical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.curation.TestCurationFactory;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.Gender;
import com.hartwig.actin.clinical.datamodel.InfectionStatus;
import com.hartwig.actin.clinical.datamodel.Intolerance;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.MedicationStatus;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.Surgery;
import com.hartwig.actin.clinical.datamodel.SurgeryStatus;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.clinical.datamodel.VitalFunction;
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory;
import com.hartwig.actin.clinical.feed.TestFeedFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ClinicalRecordsFactoryTest {

    private static final String TEST_PATIENT = "ACTN01029999";

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canGeneratePatientIds() {
        assertEquals("ACTN01029999", ClinicalRecordsFactory.toPatientId("ACTN-01-02-9999"));
        assertEquals("ACTN01029999", ClinicalRecordsFactory.toPatientId("01-02-9999"));
    }

    @Test
    public void canCreateClinicalRecordsFromMinimalTestData() {
        List<ClinicalRecord> records = createMinimalTestClinicalRecords();

        assertEquals(1, records.size());
        assertEquals(TEST_PATIENT, records.get(0).patientId());
    }

    @Test
    public void canCreateClinicalRecordsFromProperTestData() {
        List<ClinicalRecord> records = createProperTestClinicalRecords();

        assertEquals(1, records.size());
        ClinicalRecord record = records.get(0);

        assertEquals(TEST_PATIENT, record.patientId());
        assertPatientDetails(record.patient());
        assertTumorDetails(record.tumor());
        assertClinicalStatus(record.clinicalStatus());
        assertToxicities(record.toxicities());
        assertAllergies(record.intolerances());
        assertSurgeries(record.surgeries());
        assertBodyWeights(record.bodyWeights());
        assertVitalFunctions(record.vitalFunctions());
        assertBloodTransfusions(record.bloodTransfusions());
        assertMedications(record.medications());
    }

    private static void assertPatientDetails(@NotNull PatientDetails patient) {
        assertEquals(1960, patient.birthYear());
        assertEquals(Gender.MALE, patient.gender());
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
        assertEquals(TumorStage.IV, tumor.stage());
        assertTrue(tumor.hasMeasurableDisease());
        assertTrue(tumor.hasBrainLesions());
        assertTrue(tumor.hasActiveBrainLesions());
        assertNull(tumor.hasCnsLesions());
        assertNull(tumor.hasActiveCnsLesions());
        assertFalse(tumor.hasBoneLesions());
        assertFalse(tumor.hasLiverLesions());
        assertTrue(tumor.hasLungLesions());
        assertTrue(tumor.otherLesions().contains("Abdominal"));
        assertEquals("Lymph node", tumor.biopsyLocation());
    }

    private static void assertClinicalStatus(@NotNull ClinicalStatus clinicalStatus) {
        assertEquals(0, (int) clinicalStatus.who());

        InfectionStatus infectionStatus = clinicalStatus.infectionStatus();
        assertNotNull(infectionStatus);
        assertFalse(infectionStatus.hasActiveInfection());

        ECG ecg = clinicalStatus.ecg();
        assertNotNull(ecg);
        assertTrue(ecg.hasSigAberrationLatestECG());
        assertEquals("Sinus", ecg.aberrationDescription());
    }

    private static void assertToxicities(@NotNull List<Toxicity> toxicities) {
        assertEquals(2, toxicities.size());

        Toxicity toxicity1 = findByName(toxicities, "Nausea");
        assertEquals(ToxicitySource.EHR, toxicity1.source());
        assertEquals(2, (int) toxicity1.grade());

        Toxicity toxicity2 = findByName(toxicities, "Pain");
        assertEquals(ToxicitySource.EHR, toxicity2.source());
        assertEquals(0, (int) toxicity2.grade());
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

    private static void assertAllergies(@NotNull List<Intolerance> allergies) {
        assertEquals(1, allergies.size());

        Intolerance intolerance = allergies.get(0);
        assertEquals("Pills", intolerance.name());
        assertEquals("Medication", intolerance.category());
        assertEquals("Unknown", intolerance.criticality());
    }

    private static void assertSurgeries(@NotNull List<Surgery> surgeries) {
        assertEquals(1, surgeries.size());

        Surgery surgery = surgeries.get(0);
        assertEquals(LocalDate.of(2015, 10, 10), surgery.endDate());
        assertEquals(SurgeryStatus.PLANNED, surgery.status());
    }

    private static void assertBodyWeights(@NotNull List<BodyWeight> bodyWeights) {
        assertEquals(2, bodyWeights.size());

        BodyWeight bodyWeight1 = findByDate(bodyWeights, LocalDate.of(2018, 4, 5));
        assertEquals(58.1, bodyWeight1.value(), EPSILON);
        assertEquals("kilogram", bodyWeight1.unit());

        BodyWeight bodyWeight2 = findByDate(bodyWeights, LocalDate.of(2018, 5, 5));
        assertEquals(61.1, bodyWeight2.value(), EPSILON);
        assertEquals("kilogram", bodyWeight2.unit());
    }

    @NotNull
    private static BodyWeight findByDate(@NotNull List<BodyWeight> bodyWeights, @NotNull LocalDate dateToFind) {
        for (BodyWeight bodyWeight : bodyWeights) {
            if (bodyWeight.date().equals(dateToFind)) {
                return bodyWeight;
            }
        }

        throw new IllegalStateException("Could not find body weight with date '" + dateToFind + "'");
    }

    private static void assertVitalFunctions(@NotNull List<VitalFunction> vitalFunctions) {
        assertEquals(1, vitalFunctions.size());

        VitalFunction vitalFunction = vitalFunctions.get(0);
        assertEquals(LocalDate.of(2021, 2, 27), vitalFunction.date());
        assertEquals(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE, vitalFunction.category());
        assertEquals("systolic", vitalFunction.subcategory());
        assertEquals(120, vitalFunction.value(), EPSILON);
        assertEquals("mm[Hg]", vitalFunction.unit());
    }

    private static void assertBloodTransfusions(@NotNull List<BloodTransfusion> bloodTransfusions) {
        assertEquals(1, bloodTransfusions.size());

        BloodTransfusion bloodTransfusion = bloodTransfusions.get(0);
        assertEquals(LocalDate.of(2020, 7, 7), bloodTransfusion.date());
        assertEquals("Translated product", bloodTransfusion.product());
    }

    private static void assertMedications(@NotNull List<Medication> medications) {
        assertEquals(1, medications.size());

        Medication medication = medications.get(0);
        assertEquals("Paracetamol", medication.name());
        assertEquals(Sets.newHashSet("Acetanilide derivatives"), medication.categories());
        assertEquals(MedicationStatus.ACTIVE, medication.status());
        assertEquals(50, medication.dosageMin(), EPSILON);
        assertEquals(60, medication.dosageMax(), EPSILON);
        assertEquals("mg", medication.dosageUnit());
        assertEquals("day", medication.frequencyUnit());
        assertFalse(medication.ifNeeded());
        assertEquals(LocalDate.of(2019, 2, 2), medication.startDate());
        assertEquals(LocalDate.of(2019, 4, 4), medication.stopDate());
    }

    @NotNull
    private static List<ClinicalRecord> createMinimalTestClinicalRecords() {
        return new ClinicalRecordsFactory(TestFeedFactory.createMinimalTestFeedModel(),
                TestCurationFactory.createMinimalTestCurationModel()).create();
    }

    @NotNull
    private static List<ClinicalRecord> createProperTestClinicalRecords() {
        return new ClinicalRecordsFactory(TestFeedFactory.createProperTestFeedModel(),
                TestCurationFactory.createProperTestCurationModel()).create();
    }
}