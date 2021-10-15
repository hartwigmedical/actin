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
import com.google.common.io.Resources;
import com.hartwig.actin.datamodel.clinical.Allergy;
import com.hartwig.actin.datamodel.clinical.BloodTransfusion;
import com.hartwig.actin.datamodel.clinical.CancerRelatedComplication;
import com.hartwig.actin.datamodel.clinical.ImmutableAllergy;
import com.hartwig.actin.datamodel.clinical.ImmutableBloodTransfusion;
import com.hartwig.actin.datamodel.clinical.ImmutableCancerRelatedComplication;
import com.hartwig.actin.datamodel.clinical.ImmutableLabValue;
import com.hartwig.actin.datamodel.clinical.ImmutableMedication;
import com.hartwig.actin.datamodel.clinical.LabValue;
import com.hartwig.actin.datamodel.clinical.Medication;
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition;
import com.hartwig.actin.datamodel.clinical.PriorSecondPrimary;
import com.hartwig.actin.datamodel.clinical.PriorTumorTreatment;
import com.hartwig.actin.datamodel.clinical.Toxicity;
import com.hartwig.actin.datamodel.clinical.ToxicitySource;
import com.hartwig.actin.datamodel.clinical.TumorDetails;

import org.apache.logging.log4j.util.Strings;
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
                model.curatePriorSecondPrimaries(Lists.newArrayList("Breast cancer 2018", "no a second primary", "cannot curate"));

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
        assertTrue(cancerRelatedComplications.contains(ImmutableCancerRelatedComplication.builder().name("no curation").build()));

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
    public void canCurateOtherLesions() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        assertNull(model.curateBiopsyLocation(null));

        List<String> noOtherLesions = Lists.newArrayList("not a lesion");
        assertNull(model.curateOtherLesions(noOtherLesions));

        List<String> otherLesions = Lists.newArrayList("lever", "not a lesion", "no curation needed");
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
    public void canCurateECGAberrations() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        assertEquals("Cleaned aberration", model.curateAberrationECG("Weird aberration"));
        assertEquals("No curation needed", model.curateAberrationECG("No curation needed"));
        assertNull(model.curateAberrationECG(null));

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
    public void canAnnotateWithMedicationType() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        Medication proper = ImmutableMedication.builder().name("Paracetamol").type(Strings.EMPTY).build();
        Medication annotatedProper = model.annotateWithMedicationType(proper);
        assertEquals("Acetanilide derivatives", annotatedProper.type());

        Medication empty = ImmutableMedication.builder().name(Strings.EMPTY).type(Strings.EMPTY).build();
        Medication annotatedEmpty = model.annotateWithMedicationType(empty);
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

        Allergy test = ImmutableAllergy.builder().name("Naam").category(Strings.EMPTY).criticality(Strings.EMPTY).build();

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