package com.hartwig.actin.algo.soc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
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
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;
import com.hartwig.actin.treatment.datamodel.Treatment;
import com.hartwig.actin.treatment.datamodel.TreatmentComponent;

import org.junit.Ignore;
import org.junit.Test;

public class RecommendationEngineTest {

    @Test
    public void recommendCombinationChemotherapiesBeforeMonotherapies() {
        List<Treatment> recommendedTreatments = getTypicalTreatmentResults().collect(Collectors.toList());
        List<Treatment> multitherapiesAtStart = recommendedTreatments.stream().takeWhile(treatment -> treatment.components().size() > 1)
                .collect(Collectors.toList());
        List<Treatment> monotherapiesAtEnd = recommendedTreatments.stream().dropWhile(treatment -> treatment.components().size() > 1)
                .filter(treatment -> treatment.components().size() == 1)
                .collect(Collectors.toList());

        assertTrue(multitherapiesAtStart.size() > 1);
        assertTrue(monotherapiesAtEnd.size() > 1);
        assertEquals(recommendedTreatments.size(), multitherapiesAtStart.size() + monotherapiesAtEnd.size());

    }

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
        assertTrue(getTreatmentResultsForPatient(patientRecordWithHistory(Stream.of(TreatmentFactory.TREATMENT_CAPOX)))
                .noneMatch(treatment -> treatment.name().equalsIgnoreCase(TreatmentFactory.TREATMENT_FOLFIRI)));
    }

    @Test
    public void shouldNotRecommendFolfiriAterFolfox() {
        assertTrue(getTreatmentResultsForPatient(patientRecordWithHistory(Stream.of(TreatmentFactory.TREATMENT_FOLFOX)))
                .noneMatch(treatment -> treatment.name().equalsIgnoreCase(TreatmentFactory.TREATMENT_FOLFIRI)));
    }

    @Test
    public void shouldNotRecommendTheSameChemotherapyAgain() {
        Stream.of("5-FU", "Capecitabine", "Irinotecan", "Oxaliplatin", TreatmentFactory.TREATMENT_CAPOX,
                        TreatmentFactory.TREATMENT_FOLFIRI, TreatmentFactory.TREATMENT_FOLFIRINOX, TreatmentFactory.TREATMENT_FOLFOX)
                .forEach(treatment -> assertTrue(getTreatmentResultsForPatient(patientRecordWithHistory(Stream.of(treatment)))
                                .noneMatch(t -> t.name().equalsIgnoreCase(treatment))));
    }

    @Test
    @Ignore
    public void shouldOnlyRecommendFluoropyrimidineAndBevacizumabCombinationsForPatientsUnfitForCombinationChemotherapy() {
        // TODO: Provide patient unfit for combination chemotherapy
        assertTrue(getTypicalTreatmentResults().filter(treatment -> treatment.components().size() > 1)
                .allMatch(treatment -> treatment.components().size() == 2
                        && treatment.components().contains(TreatmentComponent.BEVACIZUMAB)
                        && (treatment.components().contains(TreatmentComponent.FLUOROURACIL)
                        || treatment.components().contains(TreatmentComponent.CAPECITABINE))));
    }

    /*
    Targeted therapy: Anti-EGFR (Cetuximab or Pantitumumab) +/- Chemotherapy
Requirements: wildtype KRAS, wildtype NRAS, wildtype BRAF V600E and left-sided
Anti-EGFR can be administered in second line with chemotherapy or as monotherapy in second or later treatment lines
Monotherapy may be preferred as it gives less toxicity.
No preference between Cetuximab or Panitumumab.

Chemotherapy: Trifluridine + Tipiracil (“Lonsurf”)
Not necessarily always the last treatment line; e.g. if patient has already received capecitabine and does not want chemotherapy intravenously
Effect of this drug is not great (limited gain in survival). A patient can be considered to have “exhausted SOC” when he/she has not received this treatment

Other treatment options:
In case of MSI (MMR-deficient), the first treatment line is Pembrolizumab (immunotherapy)
Pembrolizumab can be administered in a later treatment line as well, after having received 5-FU
In case of BRAF V600E, after the first line Cetuximab+Encorafenib can be given


     */

    private Stream<Treatment> getTypicalTreatmentResults() {
        return getTreatmentResultsForPatient(patientRecord());
    }

    private Stream<Treatment> getTreatmentResultsForPatient(PatientRecord patientRecord) {
        DoidModel doidModel = TestDoidModelFactory.createWithOneDoidAndTerm(DoidConstants.COLORECTAL_CANCER_DOID, "colorectal cancer");
        RecommendationEngine engine = new RecommendationEngine(doidModel, ReferenceDateProviderFactory.create(patientRecord.clinical(),
                false));
        TreatmentFactory treatmentFactory = new TreatmentFactory(doidModel);
        return engine.determineAvailableTreatments(patientRecord, treatmentFactory.loadTreatments()).orElseThrow();
    }

    private void assertSpecificMonotherapyNotRecommended(TreatmentComponent monotherapy) {
        assertTrue(getTypicalTreatmentResults().noneMatch(treatment -> treatment.components().size() == 1
                && treatment.components().contains(monotherapy)));
    }

    private PatientRecord patientRecord() {
        return patientRecordWithHistory(Stream.empty());
    }

    private PatientRecord patientRecordWithHistory(Stream<String> pastChemotherapyNames) {
        PatientRecord minimal = TestDataFactory.createMinimalTestPatientRecord();
        TumorDetails tumorDetails = ImmutableTumorDetails.builder().addDoids(DoidConstants.COLORECTAL_CANCER_DOID).build();
        ClinicalRecord clinicalRecord = ImmutableClinicalRecord.builder()
                .from(minimal.clinical())
                .tumor(tumorDetails)
                .priorTumorTreatments(pastChemotherapyNames.map(treatmentName ->
                        ImmutablePriorTumorTreatment.builder()
                                .name(treatmentName)
                                .isSystemic(true)
                                .startYear(LocalDate.now().getYear())
                                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                                .build()
                        ).collect(Collectors.toSet()))
                .build();
        return ImmutablePatientRecord.builder()
                .from(minimal)
                .clinical(clinicalRecord)
                .molecular(TestMolecularFactory.createMinimalTestMolecularRecord())
                .build();
    }
}