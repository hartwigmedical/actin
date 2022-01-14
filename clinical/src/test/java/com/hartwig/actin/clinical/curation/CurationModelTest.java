package com.hartwig.actin.clinical.curation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.hartwig.actin.clinical.datamodel.Allergy;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.clinical.datamodel.CancerRelatedComplication;
import com.hartwig.actin.clinical.datamodel.ECGAberration;
import com.hartwig.actin.clinical.datamodel.ImmutableAllergy;
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion;
import com.hartwig.actin.clinical.datamodel.ImmutableCancerRelatedComplication;
import com.hartwig.actin.clinical.datamodel.ImmutableECGAberration;
import com.hartwig.actin.clinical.datamodel.ImmutableInfectionStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.ImmutableMedication;
import com.hartwig.actin.clinical.datamodel.InfectionStatus;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;
import com.hartwig.actin.clinical.datamodel.TumorDetails;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class CurationModelTest {

    private static final double EPSILON = 1.0E-10;

    private static final String CURATION_DIRECTORY = Resources.getResource("curation").getPath();

    @Test
    public void canCreateFromCurationDirectory() throws IOException {
        assertNotNull(CurationModel.fromCurationDirectory(CURATION_DIRECTORY));
    }

    @Test
    public void canCurateTumorDetails() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        TumorDetails curated = model.curateTumorDetails("Unknown", "Carcinoma");
        assertEquals("Unknown", curated.primaryTumorLocation());

        TumorDetails missing = model.curateTumorDetails("Does not", "Exist");
        assertNull(missing.primaryTumorLocation());

        model.evaluate();
    }

    @Test
    public void canOverrideKnownLesionLocations() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        TumorDetails base = TestClinicalDataFactory.createMinimalTestClinicalRecord().tumor();

        assertEquals(base, model.overrideKnownLesionLocations(base, null));
        assertEquals(base, model.overrideKnownLesionLocations(base, Lists.newArrayList("some other lesion")));

        TumorDetails liver = model.overrideKnownLesionLocations(base, Lists.newArrayList("Lever"));
        assertNull(base.hasLiverLesions());
        assertTrue(liver.hasLiverLesions());

        TumorDetails cns = model.overrideKnownLesionLocations(base, Lists.newArrayList("cns"));
        assertNull(base.hasCnsLesions());
        assertTrue(cns.hasCnsLesions());

        TumorDetails brain = model.overrideKnownLesionLocations(base, Lists.newArrayList("brain"));
        assertNull(base.hasBrainLesions());
        assertTrue(brain.hasBrainLesions());

        TumorDetails bone = model.overrideKnownLesionLocations(base, Lists.newArrayList("Bone"));
        assertNull(base.hasBoneLesions());
        assertTrue(bone.hasBoneLesions());

        model.evaluate();
    }

    @Test
    public void canCuratePriorTreatments() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        List<PriorTumorTreatment> priorTreatments =
                model.curatePriorTumorTreatments(Lists.newArrayList("Cis 2020", "no systemic treatment", "cannot curate"));

        assertEquals(1, priorTreatments.size());
        assertEquals("platinum", priorTreatments.get(0).chemoType());

        assertTrue(model.curatePriorTumorTreatments(null).isEmpty());
        model.evaluate();
    }

    @Test
    public void canCuratePriorSecondPrimaries() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        List<PriorSecondPrimary> priorSecondPrimaries =
                model.curatePriorSecondPrimaries(Lists.newArrayList("Breast cancer Jan-2018", "no a second primary", "cannot curate"));

        assertEquals(1, priorSecondPrimaries.size());
        assertEquals("Breast", priorSecondPrimaries.get(0).tumorLocation());

        assertTrue(model.curatePriorSecondPrimaries(null).isEmpty());
        model.evaluate();
    }

    @Test
    public void canCuratePriorOtherConditions() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        List<PriorOtherCondition> priorOtherConditions =
                model.curatePriorOtherConditions(Lists.newArrayList("sickness", "not a condition", "cannot curate"));

        assertEquals(1, priorOtherConditions.size());
        assertEquals("sick", priorOtherConditions.get(0).name());

        assertTrue(model.curatePriorOtherConditions(null).isEmpty());
        model.evaluate();
    }

    @Test
    public void canCurateCancerRelatedComplications() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        List<CancerRelatedComplication> cancerRelatedComplications =
                model.curateCancerRelatedComplications(Lists.newArrayList("term", "no curation"));

        assertEquals(2, cancerRelatedComplications.size());
        assertTrue(cancerRelatedComplications.contains(ImmutableCancerRelatedComplication.builder().name("curated").build()));
        assertTrue(cancerRelatedComplications.contains(ImmutableCancerRelatedComplication.builder().name("No curation").build()));

        assertTrue(model.curateCancerRelatedComplications(null).isEmpty());

        // Add "Unknown" in case field is not filled in
        assertEquals(1, model.curateCancerRelatedComplications(Lists.newArrayList()).size());

        model.evaluate();
    }

    @Test
    public void canCurateQuestionnaireToxicities() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        LocalDate date = LocalDate.of(2018, 5, 21);
        List<Toxicity> toxicities = model.curateQuestionnaireToxicities(Lists.newArrayList("neuropathy gr3", "cannot curate"), date);

        assertEquals(1, toxicities.size());

        Toxicity toxicity = toxicities.get(0);
        assertEquals("neuropathy", toxicity.name());
        assertEquals(date, toxicity.evaluatedDate());
        assertEquals(ToxicitySource.QUESTIONNAIRE, toxicity.source());
        assertEquals(3, (int) toxicity.grade());

        assertTrue(model.curateQuestionnaireToxicities(null, date).isEmpty());
        model.evaluate();
    }

    @Test
    public void canCurateECGAberrations() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        assertEquals("Cleaned aberration", model.curateECGAberration(toAberration("Weird aberration")).description());
        assertEquals("No curation needed", model.curateECGAberration(toAberration("No curation needed")).description());
        assertNull(model.curateECGAberration(toAberration("No aberration")));
        assertNull(model.curateECGAberration(null));

        model.evaluate();
    }

    @NotNull
    private static ECGAberration toAberration(@NotNull String description) {
        return ImmutableECGAberration.builder().hasSigAberrationLatestECG(true).description(description).build();
    }

    @Test
    public void canCurateInfectionStatus() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        assertEquals("Cleaned infection", model.curateInfectionStatus(toInfection("Weird infection")).description());
        assertEquals("No curation needed", model.curateInfectionStatus(toInfection("No curation needed")).description());
        assertNull(model.curateInfectionStatus(toInfection("No infection")));
        assertNull(model.curateInfectionStatus(null));

        model.evaluate();
    }

    @NotNull
    private static InfectionStatus toInfection(@NotNull String description) {
        return ImmutableInfectionStatus.builder().hasActiveInfection(true).description(description).build();
    }

    @Test
    public void canDetermineLVEF() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        assertNull(model.determineLVEF(null));

        assertNull(model.determineLVEF(Lists.newArrayList("not an LVEF")));

        assertEquals(0.17, model.determineLVEF(Lists.newArrayList("LVEF 0.17")), EPSILON);

        model.evaluate();
    }

    @Test
    public void canCurateOtherLesions() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        assertNull(model.curateOtherLesions(null));

        List<String> notALesion = Lists.newArrayList("not a lesion");
        assertTrue(model.curateOtherLesions(notALesion).isEmpty());

        List<String> noOtherLesions = Lists.newArrayList("No");
        assertTrue(model.curateOtherLesions(noOtherLesions).isEmpty());

        List<String> otherLesions = Lists.newArrayList("lymph node", "not a lesion", "no curation needed");
        assertEquals(2, model.curateOtherLesions(otherLesions).size());

        model.evaluate();
    }

    @Test
    public void canCurateBiopsyLocation() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        assertEquals("Liver", model.curateBiopsyLocation("lever"));
        assertTrue(model.curateBiopsyLocation("Not a lesion").isEmpty());
        assertEquals("No curation needed", model.curateBiopsyLocation("No curation needed"));
        assertNull(model.curateBiopsyLocation(null));

        model.evaluate();
    }

    @Test
    public void canCurateMedicationDosage() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        Medication medication = model.curateMedicationDosage("50-60 mg per day");
        assertNotNull(medication);
        assertEquals(50, medication.dosageMin(), EPSILON);
        assertEquals(60, medication.dosageMax(), EPSILON);
        assertEquals("mg", medication.dosageUnit());
        assertEquals(1, medication.frequency(), EPSILON);
        assertEquals("day", medication.frequencyUnit());
        assertFalse(medication.ifNeeded());

        assertNull(model.curateMedicationDosage("does not exist"));
        model.evaluate();
    }

    @Test
    public void canAnnotateWithMedicationCategory() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        Medication proper = ImmutableMedication.builder().name("Paracetamol").build();
        Medication annotatedProper = model.annotateWithMedicationCategory(proper);
        assertEquals(Sets.newHashSet("Acetanilide derivatives"), annotatedProper.categories());

        Medication empty = ImmutableMedication.builder().name(Strings.EMPTY).build();
        Medication annotatedEmpty = model.annotateWithMedicationCategory(empty);
        assertEquals(empty, annotatedEmpty);

        model.evaluate();
    }

    @Test
    public void canTranslateLaboratoryValues() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        LabValue test = ImmutableLabValue.builder()
                .date(LocalDate.of(2020, 1, 1))
                .code("CO")
                .name("naam")
                .comparator(Strings.EMPTY)
                .value(0D)
                .unit(Strings.EMPTY)
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
    public void canTranslateAllergies() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        Allergy test = ImmutableAllergy.builder()
                .name("Naam")
                .category(Strings.EMPTY)
                .clinicalStatus(Strings.EMPTY)
                .verificationStatus(Strings.EMPTY)
                .criticality(Strings.EMPTY)
                .build();

        Allergy translated = model.translateAllergy(test);
        assertEquals("Name", translated.name());

        Allergy notExisting = ImmutableAllergy.builder().from(test).name("Does not exist").build();
        Allergy notExistingTranslated = model.translateAllergy(notExisting);
        assertEquals("Does not exist", notExistingTranslated.name());

        model.evaluate();
    }

    @Test
    public void canTranslateBloodTransfusions() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        BloodTransfusion test = ImmutableBloodTransfusion.builder().date(LocalDate.of(2019, 9, 9)).product("Product").build();

        BloodTransfusion translated = model.translateBloodTransfusion(test);
        assertEquals("Translated product", translated.product());

        BloodTransfusion notExisting = ImmutableBloodTransfusion.builder().from(test).product("does not exist").build();
        BloodTransfusion notExistingTranslated = model.translateBloodTransfusion(notExisting);
        assertEquals("does not exist", notExistingTranslated.product());

        model.evaluate();
    }
}