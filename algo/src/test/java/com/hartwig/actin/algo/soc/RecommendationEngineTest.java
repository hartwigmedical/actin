package com.hartwig.actin.algo.soc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.PatientRecordFactory;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.calendar.ReferenceDateProviderTestFactory;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.algo.soc.datamodel.EvaluatedTreatment;
import com.hartwig.actin.algo.soc.datamodel.Treatment;
import com.hartwig.actin.algo.soc.datamodel.TreatmentComponent;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;
import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class RecommendationEngineTest {

    @Test
    public void shouldNotRecommendCapecitabineCombinedWithIrinotecan() {
        assertTrue(getTypicalTreatmentResults().noneMatch(treatment -> treatment.components().contains(TreatmentComponent.CAPECITABINE)
                && treatment.components().contains(TreatmentComponent.IRINOTECAN)));
    }

    @Test
    public void shouldNotRecommendOxaliplatinMonotherapy() {
        assertSpecificMonotherapyNotRecommended(TreatmentComponent.OXALIPLATIN);
    }

    @Test
    public void shouldNotRecommendBevacizumabMonotherapy() {
        assertSpecificMonotherapyNotRecommended(TreatmentComponent.BEVACIZUMAB);
    }

    @Test
    public void shouldNotRecommendFolfiriAterCapox() {
        assertTrue(getTreatmentResultsForPatient(patientRecordWithChemoHistory(List.of(TreatmentDB.TREATMENT_CAPOX))).noneMatch(treatment -> treatment.name()
                .equalsIgnoreCase(TreatmentDB.TREATMENT_FOLFIRI)));
    }

    @Test
    public void shouldNotRecommendFolfiriAterFolfox() {
        assertTrue(getTreatmentResultsForPatient(patientRecordWithChemoHistory(List.of(TreatmentDB.TREATMENT_FOLFOX))).noneMatch(treatment -> treatment.name()
                .equalsIgnoreCase(TreatmentDB.TREATMENT_FOLFIRI)));
    }

    @Test
    public void shouldNotRecommendTheSameChemotherapyAgain() {
        Stream.of("5-FU",
                        "Capecitabine",
                        "Irinotecan",
                        "Oxaliplatin",
                        TreatmentDB.TREATMENT_CAPOX,
                        TreatmentDB.TREATMENT_FOLFIRI,
                        TreatmentDB.TREATMENT_FOLFIRINOX,
                        TreatmentDB.TREATMENT_FOLFOX)
                .forEach(treatment -> assertTrue(getTreatmentResultsForPatient(patientRecordWithChemoHistory(List.of(treatment))).noneMatch(
                        t -> t.name().equalsIgnoreCase(treatment))));
    }

    @Test
    public void shouldRecommendAntiEGFRTherapyForPatientsMatchingMolecularCriteria() {
        List<String> firstLineChemotherapies = List.of(TreatmentDB.TREATMENT_CAPOX);
        assertAntiEGFRTreatmentCount(getTreatmentResultsForPatient(patientRecordWithHistoryAndMolecular(firstLineChemotherapies,
                Collections.emptyList(),
                TestMolecularFactory.createProperTestMolecularRecord())), 0);
        assertAntiEGFRTreatmentCount(getTreatmentResultsForPatient(patientRecordWithChemoHistory(firstLineChemotherapies)), 18);
    }

    @Test
    public void shouldNotRecommendAntiEGFRTherapyForPatientsMatchingMolecularCriteriaButWithRightSidedTumor() {
        List<String> firstLineChemotherapies = List.of(TreatmentDB.TREATMENT_CAPOX);
        assertAntiEGFRTreatmentCount(getTreatmentResultsForPatient(patientRecordWithHistoryAndMolecular(firstLineChemotherapies,
                Collections.emptyList(),
                TestMolecularFactory.createMinimalTestMolecularRecord(),
                "Ascending colon")), 0);
    }

    private void assertAntiEGFRTreatmentCount(Stream<Treatment> treatmentResults, int count) {
        long numMatchingTreatments =
                treatmentResults.filter(treatment -> treatment.name().startsWith(TreatmentDB.TREATMENT_CETUXIMAB) || treatment.name()
                                .startsWith(TreatmentDB.TREATMENT_PANITUMUMAB))
                        .filter(treatment -> !treatment.name().contains("Encorafenib"))
                        .distinct()
                        .count();

        assertEquals(count, numMatchingTreatments);
    }

    @Test
    public void shouldRecommendPembrolizumabForMSI() {
        assertFalse(getTypicalTreatmentResults().anyMatch(treatment -> treatment.name().equals(TreatmentDB.TREATMENT_PEMBROLIZUMAB)));

        Variant variant = TestVariantFactory.builder().gene("MLH1").isReportable(true).isBiallelic(true).build();

        MolecularRecord minimal = TestMolecularFactory.createMinimalTestMolecularRecord();
        MolecularRecord molecularRecord = ImmutableMolecularRecord.builder()
                .from(minimal)
                .characteristics(ImmutableMolecularCharacteristics.builder()
                        .from(minimal.characteristics())
                        .isMicrosatelliteUnstable(true)
                        .build())
                .drivers(ImmutableMolecularDrivers.builder().from(minimal.drivers()).addVariants(variant).build())
                .build();

        assertTrue(getTreatmentResultsForPatient(patientRecordWithHistoryAndMolecular(Collections.emptyList(),
                Collections.emptyList(),
                molecularRecord)).anyMatch(treatment -> treatment.name().equals(TreatmentDB.TREATMENT_PEMBROLIZUMAB)));
    }

    @Test
    public void shouldRecommendCetuximabAndEncorafenibForBRAFV600E() {
        List<String> firstLineChemotherapies = List.of(TreatmentDB.TREATMENT_CAPOX);
        assertFalse(getTreatmentResultsForPatient(patientRecordWithChemoHistory(firstLineChemotherapies)).anyMatch(treatment -> treatment.name()
                .equals("Cetuximab + Encorafenib")));

        assertTrue(getTreatmentResultsForPatient(patientRecordWithHistoryAndMolecular(firstLineChemotherapies,
                Collections.emptyList(),
                TestMolecularFactory.createProperTestMolecularRecord())).anyMatch(treatment -> treatment.name()
                .equals("Cetuximab + Encorafenib")));
    }

    @Test
    public void shouldRecommendLonsurfAfterChemoAndTargetedTherapy() {
        PatientRecord record = patientRecordWithHistoryAndMolecular(List.of(TreatmentDB.TREATMENT_CAPOX),
                List.of(TreatmentDB.TREATMENT_PANITUMUMAB),
                TestMolecularFactory.createMinimalTestMolecularRecord());
        assertTrue(getTreatmentResultsForPatient(record).anyMatch(treatment -> treatment.name().equals(TreatmentDB.TREATMENT_LONSURF)));
    }

    @Test
    public void shouldNotRecommendLonsurfAfterTrifluridine() {
        PatientRecord record = patientRecordWithHistoryAndMolecular(List.of(TreatmentDB.TREATMENT_CAPOX, "trifluridine"),
                List.of(TreatmentDB.TREATMENT_PANITUMUMAB),
                TestMolecularFactory.createMinimalTestMolecularRecord());
        assertFalse(getTreatmentResultsForPatient(record).anyMatch(treatment -> treatment.name().equals(TreatmentDB.TREATMENT_LONSURF)));
    }

    @Test
    public void shouldThrowExceptionIfPatientDoesNotHaveColorectalCancer() {
        assertThrows(IllegalArgumentException.class, () -> getTreatmentResultsForPatient(TestDataFactory.createMinimalTestPatientRecord()));
    }

    @Test
    public void shouldThrowExceptionIfPatientHasExcludedDoid() {
        Stream.of("5777", "169", "1800")
                .forEach(doid -> assertThrows(IllegalArgumentException.class,
                        () -> getTreatmentResultsForPatient(patientRecordWithTumorDoids(doid))));
    }

    @Test
    public void shouldNotRecommendMultiChemotherapyForPatientsAged75OrOlder() {
        ClinicalRecord minimalClinical = TestClinicalFactory.createMinimalTestClinicalRecord();
        TumorDetails tumorDetails = ImmutableTumorDetails.builder().addDoids(DoidConstants.COLORECTAL_CANCER_DOID).build();

        PatientRecord patientRecord = ImmutablePatientRecord.copyOf(TestDataFactory.createMinimalTestPatientRecord())
                .withClinical(ImmutableClinicalRecord.copyOf(minimalClinical)
                        .withTumor(tumorDetails)
                        .withPatient(ImmutablePatientDetails.copyOf(minimalClinical.patient())
                                .withBirthYear(LocalDate.now().minusYears(76).getYear())));

        assertMultiChemotherapyNotRecommended(patientRecord);
    }

    @Test
    public void shouldNotRecommendMultiChemotherapyForPatientsWithWHOStatusGreaterThan1() {
        ClinicalRecord minimalClinical = TestClinicalFactory.createMinimalTestClinicalRecord();
        TumorDetails tumorDetails = ImmutableTumorDetails.builder().addDoids(DoidConstants.COLORECTAL_CANCER_DOID).build();

        PatientRecord patientRecord = ImmutablePatientRecord.copyOf(TestDataFactory.createMinimalTestPatientRecord())
                .withClinical(ImmutableClinicalRecord.copyOf(minimalClinical)
                        .withTumor(tumorDetails)
                        .withClinicalStatus(ImmutableClinicalStatus.copyOf(minimalClinical.clinicalStatus()).withWho(2)));

        assertMultiChemotherapyNotRecommended(patientRecord);
    }

    private static Stream<Treatment> getTypicalTreatmentResults() {
        return getTreatmentResultsForPatient(patientRecord());
    }

    private static Stream<Treatment> getTreatmentResultsForPatient(PatientRecord patientRecord) {
        DoidModel doidModel = TestDoidModelFactory.createWithOneDoidAndTerm(DoidConstants.COLORECTAL_CANCER_DOID, "colorectal cancer");
        RecommendationEngine engine = new RecommendationEngine(doidModel, ReferenceDateProviderTestFactory.createCurrentDateProvider());
        return engine.determineAvailableTreatments(patientRecord, TreatmentDB.loadTreatments()).map(EvaluatedTreatment::treatment);
    }

    private static void assertSpecificMonotherapyNotRecommended(TreatmentComponent monotherapy) {
        assertTrue(getTypicalTreatmentResults().noneMatch(treatment -> treatment.components().size() == 1 && treatment.components()
                .contains(monotherapy)));
    }

    private static void assertMultiChemotherapyNotRecommended(PatientRecord patientRecord) {
        Set<TreatmentComponent> chemotherapyComponents = Set.of(TreatmentComponent.FLUOROURACIL,
                TreatmentComponent.OXALIPLATIN,
                TreatmentComponent.IRINOTECAN,
                TreatmentComponent.CAPECITABINE);

        assertTrue(getTreatmentResultsForPatient(patientRecord).map(treatment -> treatment.components()
                .stream()
                .map(component -> chemotherapyComponents.contains(component) ? 1 : 0)
                .reduce(0, Integer::sum)).noneMatch(numChemoComponents -> numChemoComponents > 1));
    }

    private static PatientRecord patientRecord() {
        return patientRecordWithChemoHistory(Collections.emptyList());
    }

    private static PatientRecord patientRecordWithTumorDoids(String tumorDoid) {
        PatientRecord minimal = TestDataFactory.createMinimalTestPatientRecord();
        TumorDetails tumorDetails = ImmutableTumorDetails.builder().addDoids(DoidConstants.COLORECTAL_CANCER_DOID, tumorDoid).build();
        ClinicalRecord clinicalRecord = ImmutableClinicalRecord.builder().from(minimal.clinical()).tumor(tumorDetails).build();
        return ImmutablePatientRecord.builder().from(minimal).clinical(clinicalRecord).build();
    }

    private static PatientRecord patientRecordWithChemoHistory(List<String> pastChemotherapyNames) {
        return patientRecordWithHistoryAndMolecular(pastChemotherapyNames,
                Collections.emptyList(),
                TestMolecularFactory.createMinimalTestMolecularRecord());
    }

    private static PatientRecord patientRecordWithHistoryAndMolecular(List<String> pastChemotherapyNames,
            List<String> pastTargetedTherapyNames, MolecularRecord molecularRecord) {
        return patientRecordWithHistoryAndMolecular(pastChemotherapyNames, pastTargetedTherapyNames, molecularRecord, null);
    }

    private static PatientRecord patientRecordWithHistoryAndMolecular(List<String> pastChemotherapyNames,
            List<String> pastTargetedTherapyNames, MolecularRecord molecularRecord, @Nullable String tumorSubLocation) {
        TumorDetails tumorDetails = ImmutableTumorDetails.builder()
                .addDoids(DoidConstants.COLORECTAL_CANCER_DOID)
                .primaryTumorSubLocation(tumorSubLocation)
                .build();
        ClinicalRecord clinicalRecord = ImmutableClinicalRecord.builder()
                .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                .tumor(tumorDetails)
                .priorTumorTreatments(Stream.concat(priorTreatmentStreamFromNames(pastChemotherapyNames, TreatmentCategory.CHEMOTHERAPY),
                                priorTreatmentStreamFromNames(pastTargetedTherapyNames, TreatmentCategory.TARGETED_THERAPY))
                        .collect(Collectors.toSet()))
                .build();
        return PatientRecordFactory.fromInputs(clinicalRecord, molecularRecord);
    }

    private static Stream<ImmutablePriorTumorTreatment> priorTreatmentStreamFromNames(List<String> names, TreatmentCategory category) {
        return names.stream()
                .map(treatmentName -> ImmutablePriorTumorTreatment.builder()
                        .name(treatmentName)
                        .isSystemic(true)
                        .startYear(LocalDate.now().getYear())
                        .addCategories(category)
                        .build());
    }
}