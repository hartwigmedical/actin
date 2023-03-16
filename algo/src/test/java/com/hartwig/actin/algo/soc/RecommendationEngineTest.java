package com.hartwig.actin.algo.soc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.calendar.ReferenceDateProviderFactory;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
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
import com.hartwig.actin.treatment.datamodel.Treatment;
import com.hartwig.actin.treatment.datamodel.TreatmentComponent;

import org.junit.Ignore;
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
        assertTrue(getTreatmentResultsForPatient(patientRecordWithHistory(List.of(TreatmentDB.TREATMENT_CAPOX))).noneMatch(treatment -> treatment.name()
                .equalsIgnoreCase(TreatmentDB.TREATMENT_FOLFIRI)));
    }

    @Test
    public void shouldNotRecommendFolfiriAterFolfox() {
        assertTrue(getTreatmentResultsForPatient(patientRecordWithHistory(List.of(TreatmentDB.TREATMENT_FOLFOX))).noneMatch(treatment -> treatment.name()
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
                .forEach(treatment -> assertTrue(getTreatmentResultsForPatient(patientRecordWithHistory(List.of(treatment))).noneMatch(t -> t.name()
                        .equalsIgnoreCase(treatment))));
    }

    @Test
    public void shouldRecommendAntiEGFRTherapyForPatientsMatchingMolecularCriteria() {
        List<String> firstLineChemotherapies = List.of(TreatmentDB.TREATMENT_CAPOX);
        assertAntiEGFRTreatmentCount(getTreatmentResultsForPatient(patientRecordWithHistoryAndMolecular(firstLineChemotherapies,
                TestMolecularFactory.createProperTestMolecularRecord())), 0);
        assertAntiEGFRTreatmentCount(getTreatmentResultsForPatient(patientRecordWithHistory(firstLineChemotherapies)), 18);
    }

    private void assertAntiEGFRTreatmentCount(Stream<Treatment> treatmentResults, int count) {
        long numMatchingTreatments = treatmentResults.filter(treatment -> treatment.name().startsWith("Cetuximab") || treatment.name()
                .startsWith("Pantitumumab")).filter(treatment -> !treatment.name().contains("Encorafenib")).distinct().count();

        assertEquals(count, numMatchingTreatments);
    }

    @Test
    public void shouldRecommendPembrolizumabForMSI() {
        assertFalse(getTypicalTreatmentResults().anyMatch(treatment -> treatment.name().equals("Pembrolizumab")));

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

        assertTrue(getTreatmentResultsForPatient(patientRecordWithHistoryAndMolecular(Collections.emptyList(), molecularRecord)).anyMatch(
                treatment -> treatment.name().equals("Pembrolizumab")));
    }

    @Test
    public void shouldRecommendCetuximabAndEncorafenibForBRAFV600E() {
        List<String> firstLineChemotherapies = List.of(TreatmentDB.TREATMENT_CAPOX);
        assertFalse(getTreatmentResultsForPatient(patientRecordWithHistory(firstLineChemotherapies)).anyMatch(treatment -> treatment.name()
                .equals("Cetuximab + Encorafenib")));

        assertTrue(getTreatmentResultsForPatient(patientRecordWithHistoryAndMolecular(firstLineChemotherapies,
                TestMolecularFactory.createProperTestMolecularRecord())).anyMatch(treatment -> treatment.name()
                .equals("Cetuximab + Encorafenib")));
    }

    @Test
    @Ignore
    public void shouldOnlyRecommendFluoropyrimidineAndBevacizumabCombinationsForPatientsUnfitForCombinationChemotherapy() {
        // TODO: Provide patient unfit for combination chemotherapy
        assertTrue(getTypicalTreatmentResults().filter(treatment -> treatment.components().size() > 1)
                .allMatch(treatment -> treatment.components().size() == 2 && treatment.components().contains(TreatmentComponent.BEVACIZUMAB)
                        && (treatment.components().contains(TreatmentComponent.FLUOROURACIL) || treatment.components()
                        .contains(TreatmentComponent.CAPECITABINE))));
    }

    private Stream<Treatment> getTypicalTreatmentResults() {
        return getTreatmentResultsForPatient(patientRecord());
    }

    private Stream<Treatment> getTreatmentResultsForPatient(PatientRecord patientRecord) {
        DoidModel doidModel = TestDoidModelFactory.createWithOneDoidAndTerm(DoidConstants.COLORECTAL_CANCER_DOID, "colorectal cancer");
        RecommendationEngine engine =
                new RecommendationEngine(doidModel, ReferenceDateProviderFactory.create(patientRecord.clinical(), false));
        return engine.determineAvailableTreatments(patientRecord, TreatmentDB.loadTreatments());
    }

    private void assertSpecificMonotherapyNotRecommended(TreatmentComponent monotherapy) {
        assertTrue(getTypicalTreatmentResults().noneMatch(treatment -> treatment.components().size() == 1 && treatment.components()
                .contains(monotherapy)));
    }

    private PatientRecord patientRecord() {
        return patientRecordWithHistory(Collections.emptyList());
    }

    private PatientRecord patientRecordWithHistory(List<String> pastChemotherapyNames) {
        return patientRecordWithHistoryAndMolecular(pastChemotherapyNames, TestMolecularFactory.createMinimalTestMolecularRecord());
    }

    private PatientRecord patientRecordWithHistoryAndMolecular(List<String> pastChemotherapyNames, MolecularRecord molecularRecord) {
        PatientRecord minimal = TestDataFactory.createMinimalTestPatientRecord();
        TumorDetails tumorDetails = ImmutableTumorDetails.builder().addDoids(DoidConstants.COLORECTAL_CANCER_DOID).build();
        ClinicalRecord clinicalRecord = ImmutableClinicalRecord.builder()
                .from(minimal.clinical())
                .tumor(tumorDetails)
                .priorTumorTreatments(pastChemotherapyNames.stream()
                        .map(treatmentName -> ImmutablePriorTumorTreatment.builder()
                                .name(treatmentName)
                                .isSystemic(true)
                                .startYear(LocalDate.now().getYear())
                                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                                .build())
                        .collect(Collectors.toSet()))
                .build();
        return ImmutablePatientRecord.builder().from(minimal).clinical(clinicalRecord).molecular(molecularRecord).build();
    }
}