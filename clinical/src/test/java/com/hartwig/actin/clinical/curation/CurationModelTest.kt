package com.hartwig.actin.clinical.curation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion;
import com.hartwig.actin.clinical.datamodel.ImmutableECG;
import com.hartwig.actin.clinical.datamodel.ImmutableInfectionStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableIntolerance;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity;
import com.hartwig.actin.clinical.datamodel.InfectionStatus;
import com.hartwig.actin.clinical.datamodel.Intolerance;
import com.hartwig.actin.clinical.datamodel.LabUnit;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.MedicationStatus;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class CurationModelTest {

    private static final double EPSILON = 1.0E-10;

    private static final String CURATION_DIRECTORY = Resources.getResource("curation").getPath();

    private final CurationModel model = TestCurationFactory.createProperTestCurationModel();

    @Test
    public void canCreateFromCurationDirectory() throws IOException {
        assertNotNull(CurationModel.create(CURATION_DIRECTORY, TestDoidModelFactory.createMinimalTestDoidModel()));
    }

    @Test
    public void canCurateTumorDetails() {
        TumorDetails curated = model.curateTumorDetails("Unknown", "Carcinoma");
        assertEquals("Unknown", curated.primaryTumorLocation());

        TumorDetails missing = model.curateTumorDetails("Does not", "Exist");
        assertNull(missing.primaryTumorLocation());

        model.evaluate();
    }

    @Test
    public void shouldNotOverrideLesionLocationsForUnknownBiopsiesAndLesions() {
        TumorDetails base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor();
        assertEquals(base, model.overrideKnownLesionLocations(base, null, null));
        assertEquals(base, model.overrideKnownLesionLocations(base, "biopsy location", Lists.newArrayList("some other lesion")));
    }

    @Test
    public void shouldOverrideHasLiverLesionsWhenListedInOtherLesions() {
        TumorDetails base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor();
        assertNull(base.hasLiverLesions());
        TumorDetails liverOther = model.overrideKnownLesionLocations(base, null, Lists.newArrayList("Lever"));
        assertEquals(true, liverOther.hasLiverLesions());
        model.evaluate();
    }

    @Test
    public void shouldOverrideHasLiverLesionsWhenListedAsBiopsy() {
        TumorDetails base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor();
        TumorDetails liverBiopsy = model.overrideKnownLesionLocations(base, "lever", null);
        assertEquals(true, liverBiopsy.hasLiverLesions());
        model.evaluate();
    }

    @Test
    public void shouldOverrideHasCnsLesionsWhenListedInOtherLesions() {
        TumorDetails base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor();
        assertNull(base.hasCnsLesions());
        TumorDetails cns = model.overrideKnownLesionLocations(base, null, Lists.newArrayList("cns"));
        assertEquals(true, cns.hasCnsLesions());
        model.evaluate();
    }

    @Test
    public void shouldOverrideHasBrainLesionsWhenListedInOtherLesions() {
        TumorDetails base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor();
        assertNull(base.hasBrainLesions());
        TumorDetails brain = model.overrideKnownLesionLocations(base, null, Lists.newArrayList("brain"));
        assertEquals(true, brain.hasBrainLesions());
        model.evaluate();
    }

    @Test
    public void shouldOverrideHasLymphNodeLesionsWhenListedInOtherLesions() {
        TumorDetails base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor();
        assertNull(base.hasLymphNodeLesions());
        TumorDetails lymphNode = model.overrideKnownLesionLocations(base, null, Lists.newArrayList("lymph node"));
        assertEquals(true, lymphNode.hasLymphNodeLesions());
        model.evaluate();
    }

    @Test
    public void shouldOverrideHasBoneLesionsWhenListedInOtherLesions() {
        TumorDetails base = TestClinicalFactory.createMinimalTestClinicalRecord().tumor();
        assertNull(base.hasBoneLesions());
        TumorDetails bone = model.overrideKnownLesionLocations(base, null, Lists.newArrayList("Bone"));
        assertEquals(true, bone.hasBoneLesions());
        model.evaluate();
    }

    @Test
    public void shouldCurateTreatmentHistory() {
        List<TreatmentHistoryEntry> treatmentHistory =
                model.curateTreatmentHistory(List.of("Cis 2020 2021", "no systemic treatment", "cannot curate"));
        assertEquals(2, treatmentHistory.size());
        assertTrue(treatmentHistory.stream().anyMatch(entry -> Integer.valueOf(2020).equals(entry.startYear())));
        assertTrue(treatmentHistory.stream().anyMatch(entry -> Integer.valueOf(2021).equals(entry.startYear())));

        assertTrue(model.curatePriorTumorTreatments(null).isEmpty());
        model.evaluate();
    }

    @Test
    public void canCuratePriorTreatments() {
        List<PriorTumorTreatment> priorTreatments =
                model.curatePriorTumorTreatments(Lists.newArrayList("Cis 2020 2021", "no systemic treatment", "cannot curate"));

        assertEquals(2, priorTreatments.size());
        assertTrue(priorTreatments.stream().anyMatch(entry -> Integer.valueOf(2020).equals(entry.startYear())));
        assertTrue(priorTreatments.stream().anyMatch(entry -> Integer.valueOf(2021).equals(entry.startYear())));

        assertTrue(model.curatePriorTumorTreatments(null).isEmpty());
        model.evaluate();
    }

    @Test
    public void canCuratePriorSecondPrimaries() {
        List<PriorSecondPrimary> priorSecondPrimaries =
                model.curatePriorSecondPrimaries(Lists.newArrayList("Breast cancer Jan-2018", "cannot curate"));

        assertEquals(1, priorSecondPrimaries.size());
        assertEquals("Breast", priorSecondPrimaries.get(0).tumorLocation());

        assertTrue(model.curatePriorSecondPrimaries(null).isEmpty());
        model.evaluate();
    }

    @Test
    public void canCuratePriorOtherConditions() {
        List<PriorOtherCondition> priorOtherConditions =
                model.curatePriorOtherConditions(Lists.newArrayList("sickness", "not a condition", "cannot curate"));

        assertEquals(1, priorOtherConditions.size());
        assertEquals("sick", priorOtherConditions.get(0).name());

        assertTrue(model.curatePriorOtherConditions(null).isEmpty());
        model.evaluate();
    }

    @Test
    public void canCuratePriorMolecularTests() {
        List<PriorMolecularTest> priorMolecularTests =
                model.curatePriorMolecularTests("IHC", Lists.newArrayList("IHC ERBB2 3+", "not a molecular test"));

        assertEquals(1, priorMolecularTests.size());
        assertEquals("IHC", priorMolecularTests.get(0).test());

        assertTrue(model.curatePriorTumorTreatments(null).isEmpty());
        model.evaluate();
    }

    @Test
    public void canCurateComplications() {
        assertNull(model.curateComplications(null));
        assertNull(model.curateComplications(Lists.newArrayList()));

        List<Complication> complications = model.curateComplications(Lists.newArrayList("term", "no curation"));
        assertNotNull(complications);
        assertEquals(1, complications.size());
        assertNotNull(findComplicationByName(complications, "Curated"));

        List<Complication> ignore = model.curateComplications(Lists.newArrayList("none"));
        assertNotNull(ignore);
        assertEquals(0, ignore.size());

        List<Complication> unknown = model.curateComplications(Lists.newArrayList("unknown"));
        assertNull(unknown);

        model.evaluate();
    }

    @NotNull
    private static Complication findComplicationByName(@NotNull List<Complication> complications, @NotNull String nameToFind) {
        return complications.stream()
                .filter(complication -> complication.name().equals(nameToFind))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Could not find complication with name '" + nameToFind + "'"));
    }

    @Test
    public void canCurateQuestionnaireToxicities() {
        LocalDate date = LocalDate.of(2018, 5, 21);
        List<Toxicity> toxicities = model.curateQuestionnaireToxicities(Lists.newArrayList("neuropathy gr3", "cannot curate"), date);

        assertEquals(1, toxicities.size());

        Toxicity toxicity = toxicities.get(0);
        assertEquals("neuropathy", toxicity.name());
        assertEquals(Sets.newHashSet("neuro"), toxicity.categories());
        assertEquals(date, toxicity.evaluatedDate());
        assertEquals(ToxicitySource.QUESTIONNAIRE, toxicity.source());
        assertEquals(Integer.valueOf(3), toxicity.grade());

        assertTrue(model.curateQuestionnaireToxicities(null, date).isEmpty());
        model.evaluate();
    }

    @Test
    public void canCurateECGs() {
        assertAberrationDescription("Cleaned aberration", model.curateECG(toECG("Weird aberration")));
        assertAberrationDescription("No curation needed", model.curateECG(toECG("No curation needed")));
        assertAberrationDescription(null, model.curateECG(toECG("Yes but unknown what aberration")));
        assertNull(model.curateECG(toECG("No aberration")));
        assertNull(model.curateECG(null));

        model.evaluate();
    }

    @NotNull
    private static ECG toECG(@NotNull String aberrationDescription) {
        return ImmutableECG.builder().hasSigAberrationLatestECG(true).aberrationDescription(aberrationDescription).build();
    }

    @Test
    public void canCurateInfectionStatus() {
        assertInfectionDescription("Cleaned infection", model.curateInfectionStatus(toInfection("Weird infection")));
        assertInfectionDescription("No curation needed", model.curateInfectionStatus(toInfection("No curation needed")));
        assertInfectionDescription(null, model.curateInfectionStatus(toInfection("No Infection")));
        assertNull(model.curateInfectionStatus(null));

        model.evaluate();
    }

    @NotNull
    private static InfectionStatus toInfection(@NotNull String description) {
        return ImmutableInfectionStatus.builder().hasActiveInfection(true).description(description).build();
    }

    @Test
    public void canDetermineLVEF() {
        assertNull(model.determineLVEF(null));

        assertNull(model.determineLVEF(Lists.newArrayList("not an LVEF")));

        Double lvef = model.determineLVEF(Lists.newArrayList("LVEF 0.17"));
        assertNotNull(lvef);
        assertEquals(0.17, lvef, EPSILON);

        model.evaluate();
    }

    @Test
    public void canCurateOtherLesions() {
        assertNull(model.curateOtherLesions(null));

        List<String> notALesionCuration = model.curateOtherLesions(Lists.newArrayList("not a lesion"));
        assertNotNull(notALesionCuration);
        assertTrue(notALesionCuration.isEmpty());

        List<String> noOtherLesionsCuration = model.curateOtherLesions(Lists.newArrayList("No"));
        assertNotNull(noOtherLesionsCuration);
        assertTrue(noOtherLesionsCuration.isEmpty());

        List<String> otherLesionsCuration =
                model.curateOtherLesions(Lists.newArrayList("lymph node", "not a lesion", "no curation available"));
        assertNotNull(otherLesionsCuration);
        assertEquals(1, otherLesionsCuration.size());

        model.evaluate();
    }

    @Test
    public void canCurateBiopsyLocation() {
        assertEquals("Liver", model.curateBiopsyLocation("lever"));
        assertEquals(Strings.EMPTY, model.curateBiopsyLocation("Not a lesion"));
        assertNull(model.curateBiopsyLocation("No curation configured"));
        assertNull(model.curateBiopsyLocation(null));

        model.evaluate();
    }

    @Test
    public void canCurateMedicationDosage() {
        Medication medication = model.curateMedicationDosage("50-60 mg per day");
        assertNotNull(medication);
        assertDoubleEquals(50, medication.dosageMin());
        assertDoubleEquals(60, medication.dosageMax());
        assertEquals("mg", medication.dosageUnit());
        assertDoubleEquals(1, medication.frequency());
        assertEquals("day", medication.frequencyUnit());
        assertEquals(false, medication.ifNeeded());

        assertNull(model.curateMedicationDosage("does not exist"));
        model.evaluate();
    }

    @Test
    public void canCurateMedicationName() {
        assertNull(model.curateMedicationName(Strings.EMPTY));
        assertNull(model.curateMedicationName("does not exist"));
        assertNull(model.curateMedicationName("No medication"));

        assertEquals("A and B", model.curateMedicationName("A en B"));

        model.evaluate();
    }

    @Test
    public void canCurateMedicationCodeATC() {
        assertEquals(Strings.EMPTY, model.curateMedicationCodeATC(Strings.EMPTY));
        assertEquals("N12", model.curateMedicationCodeATC("N12"));
        assertEquals(Strings.EMPTY, model.curateMedicationCodeATC("12N"));
    }

    @Test
    public void canCurateMedicationStatus() {
        CurationModel model = TestCurationFactory.createMinimalTestCurationModel();

        assertNull(model.curateMedicationStatus(Strings.EMPTY));
        assertEquals(MedicationStatus.ACTIVE, model.curateMedicationStatus("active"));
        assertEquals(MedicationStatus.ON_HOLD, model.curateMedicationStatus("on-hold"));
        assertEquals(MedicationStatus.CANCELLED, model.curateMedicationStatus("Kuur geannuleerd"));
        assertEquals(MedicationStatus.UNKNOWN, model.curateMedicationStatus("not a status"));
    }

    @Test
    public void canAnnotateWithMedicationCategory() {
        Medication proper = TestMedicationFactory.builder().name("Paracetamol").build();
        Medication annotatedProper = model.annotateWithMedicationCategory(proper);
        assertEquals(Sets.newHashSet("Acetanilide derivatives"), annotatedProper.categories());

        Medication empty = TestMedicationFactory.builder().name(Strings.EMPTY).build();
        Medication annotatedEmpty = model.annotateWithMedicationCategory(empty);
        assertEquals(empty, annotatedEmpty);

        model.evaluate();
    }

    @Test
    public void canTranslateAdministrationRoute() {
        assertNull(model.translateAdministrationRoute(null));
        assertNull(model.translateAdministrationRoute(Strings.EMPTY));
        assertNull(model.translateAdministrationRoute("not a route"));
        assertNull(model.translateAdministrationRoute("ignore"));

        assertEquals("oral", model.translateAdministrationRoute("oraal"));
    }

    @Test
    public void canCurateIntolerances() {
        Intolerance proper = ImmutableIntolerance.builder()
                .name("Latex type 1")
                .category(Strings.EMPTY)
                .type(Strings.EMPTY)
                .clinicalStatus(Strings.EMPTY)
                .verificationStatus(Strings.EMPTY)
                .criticality(Strings.EMPTY)
                .build();

        Intolerance curatedProper = model.curateIntolerance(proper);
        assertEquals("Latex (type 1)", curatedProper.name());
        assertTrue(curatedProper.doids().contains("0060532"));

        Intolerance passThrough = ImmutableIntolerance.builder().from(proper).name("don't curate me").build();
        assertEquals(passThrough, model.curateIntolerance(passThrough));

        Intolerance withSubCategory = ImmutableIntolerance.builder().from(proper).name("Paracetamol").category("Medication").build();
        assertTrue(model.curateIntolerance(withSubCategory).subcategories().contains("Acetanilide derivatives"));

        model.evaluate();
    }

    @Test
    public void canTranslateLaboratoryValues() {
        LabValue test = ImmutableLabValue.builder()
                .date(LocalDate.of(2020, 1, 1))
                .code("CO")
                .name("naam")
                .comparator(Strings.EMPTY)
                .value(0D)
                .unit(LabUnit.NONE)
                .isOutsideRef(false)
                .build();

        LabValue translated = model.translateLabValue(test);
        assertEquals("CODE", translated.code());
        assertEquals("Name", translated.name());

        LabValue notExisting = ImmutableLabValue.builder().from(test).code("no").name("does not exist").build();
        LabValue notExistingTranslated = model.translateLabValue(notExisting);
        assertEquals("no", notExistingTranslated.code());
        assertEquals("does not exist", notExistingTranslated.name());

        model.evaluate();
    }

    @Test
    public void canTranslateToxicities() {
        Toxicity test =
                ImmutableToxicity.builder().name("Pijn").evaluatedDate(LocalDate.of(2020, 11, 11)).source(ToxicitySource.EHR).build();

        Toxicity translated = model.translateToxicity(test);
        assertEquals("Pain", translated.name());

        Toxicity notExisting = ImmutableToxicity.builder().from(test).name("something").build();
        Toxicity notExistingTranslated = model.translateToxicity(notExisting);
        assertEquals(notExisting.name(), notExistingTranslated.name());

        model.evaluate();
    }

    @Test
    public void canTranslateBloodTransfusions() {
        BloodTransfusion test = ImmutableBloodTransfusion.builder().date(LocalDate.of(2019, 9, 9)).product("Product").build();

        BloodTransfusion translated = model.translateBloodTransfusion(test);
        assertEquals("Translated product", translated.product());

        BloodTransfusion notExisting = ImmutableBloodTransfusion.builder().from(test).product("does not exist").build();
        BloodTransfusion notExistingTranslated = model.translateBloodTransfusion(notExisting);
        assertEquals("does not exist", notExistingTranslated.product());

        model.evaluate();
    }

    private void assertDoubleEquals(double expected, @Nullable Double actual) {
        assertNotNull(actual);
        assertEquals(expected, actual, EPSILON);
    }

    private void assertAberrationDescription(@Nullable String expectedDescription, @Nullable ECG curatedECG) {
        assertNotNull(curatedECG);
        assertEquals(expectedDescription, curatedECG.aberrationDescription());
    }

    private void assertInfectionDescription(@Nullable String expected, @Nullable InfectionStatus infectionStatus) {
        assertNotNull(infectionStatus);
        assertEquals(expected, infectionStatus.description());
    }
}