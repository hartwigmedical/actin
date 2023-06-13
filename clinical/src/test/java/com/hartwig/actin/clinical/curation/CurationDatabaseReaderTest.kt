package com.hartwig.actin.clinical.curation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.hartwig.actin.clinical.curation.config.ComplicationConfig;
import com.hartwig.actin.clinical.curation.config.CurationConfig;
import com.hartwig.actin.clinical.curation.config.ECGConfig;
import com.hartwig.actin.clinical.curation.config.InfectionConfig;
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig;
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig;
import com.hartwig.actin.clinical.curation.config.MedicationCategoryConfig;
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig;
import com.hartwig.actin.clinical.curation.config.MedicationNameConfig;
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig;
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig;
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig;
import com.hartwig.actin.clinical.curation.config.ToxicityConfig;
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig;
import com.hartwig.actin.clinical.curation.translation.AdministrationRouteTranslation;
import com.hartwig.actin.clinical.curation.translation.BloodTransfusionTranslation;
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation;
import com.hartwig.actin.clinical.curation.translation.ToxicityTranslation;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.treatment.Treatment;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory;
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason;
import com.hartwig.actin.clinical.datamodel.treatment.history.TherapyHistoryDetails;
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry;
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;

public class CurationDatabaseReaderTest {

    private static final double EPSILON = 1.0E-10;

    private static final String CURATION_DIRECTORY = Resources.getResource("curation").getPath();
    private final CurationDatabaseReader reader =
            new CurationDatabaseReader(TestCurationFactory.createMinimalTestCurationDatabaseValidator());
    private CurationDatabase database;

    @Before
    public void createDatabase() throws IOException {
        database = reader.read(CURATION_DIRECTORY);
    }

    @Test
    public void shouldReadPrimaryTumorConfigs() {
        List<PrimaryTumorConfig> configs = database.primaryTumorConfigs();
        assertEquals(1, configs.size());

        PrimaryTumorConfig config = configs.get(0);
        assertEquals("Unknown | Carcinoma", config.input());
        assertEquals("Unknown", config.primaryTumorLocation());
        assertEquals("CUP", config.primaryTumorSubLocation());
        assertEquals("Carcinoma", config.primaryTumorType());
        assertEquals(Strings.EMPTY, config.primaryTumorSubType());
        assertEquals(Strings.EMPTY, config.primaryTumorExtraDetails());
        assertEquals(1, config.doids().size());
        assertTrue(config.doids().contains("299"));
    }

    @Test
    public void shouldReadTreatmentHistoryEntryConfigs() {
        List<TreatmentHistoryEntryConfig> configs = database.treatmentHistoryEntryConfigs();
        assertEquals(1, configs.size());

        TreatmentHistoryEntryConfig config = find(configs, "Capecitabine/Oxaliplatin 2020-2021");
        assertFalse(config.ignore());

        TreatmentHistoryEntry curated = config.curated();
        assertNotNull(curated);
        Treatment treatment = curated.treatments().iterator().next();
        assertEquals("Capecitabine+Oxaliplatin", treatment.name());
        assertIntegerEquals(2020, curated.startYear());
        assertNull(curated.startMonth());

        TherapyHistoryDetails therapyHistoryDetails = curated.therapyHistoryDetails();
        assertNotNull(therapyHistoryDetails);
        assertIntegerEquals(2021, therapyHistoryDetails.stopYear());
        assertNull(therapyHistoryDetails.stopMonth());
        assertIntegerEquals(6, therapyHistoryDetails.cycles());
        assertEquals(TreatmentResponse.PARTIAL_RESPONSE, therapyHistoryDetails.bestResponse());
        assertEquals(StopReason.TOXICITY, therapyHistoryDetails.stopReason());
        assertEquals(Set.of(TreatmentCategory.CHEMOTHERAPY), treatment.categories());
        assertTrue(treatment.isSystemic());
        assertNull(curated.trialAcronym());
    }

    @Test
    public void shouldReadOncologicalHistoryConfigs() {
        List<OncologicalHistoryConfig> configs = database.oncologicalHistoryConfigs();
        assertEquals(1, configs.size());

        OncologicalHistoryConfig config = find(configs, "Capecitabine/Oxaliplatin 2020-2021");
        assertFalse(config.ignore());

        PriorTumorTreatment curated = config.curated();
        assertNotNull(curated);
        assertEquals("Capecitabine+Oxaliplatin", curated.name());
        assertIntegerEquals(2020, curated.startYear());
        assertNull(curated.startMonth());
        assertIntegerEquals(2021, curated.stopYear());
        assertNull(curated.stopMonth());
        assertIntegerEquals(6, curated.cycles());
        assertEquals("PR", curated.bestResponse());
        assertEquals("toxicity", curated.stopReason());
        assertEquals(Sets.newHashSet(TreatmentCategory.CHEMOTHERAPY), curated.categories());
        assertTrue(curated.isSystemic());
        assertEquals("antimetabolite,platinum", curated.chemoType());
        assertNull(curated.immunoType());
        assertNull(curated.targetedType());
        assertNull(curated.hormoneType());
        assertNull(curated.radioType());
        assertNull(curated.transplantType());
        assertNull(curated.supportiveType());
        assertNull(curated.trialAcronym());
        assertNull(curated.ablationType());
    }

    @Test
    public void shouldReadSecondPrimaryConfigs() {
        List<SecondPrimaryConfig> configs = database.secondPrimaryConfigs();
        assertEquals(1, configs.size());

        SecondPrimaryConfig config = find(configs, "basaalcelcarcinoom (2014) | 2014");
        assertFalse(config.ignore());

        PriorSecondPrimary curated = config.curated();
        assertNotNull(curated);
        assertEquals(Strings.EMPTY, curated.tumorLocation());
        assertEquals(Strings.EMPTY, curated.tumorSubLocation());
        assertEquals("Carcinoma", curated.tumorType());
        assertEquals("Basal cell carcinoma", curated.tumorSubType());
        assertEquals(Sets.newHashSet("2513"), curated.doids());
        assertIntegerEquals(2014, curated.diagnosedYear());
        assertIntegerEquals(1, curated.diagnosedMonth());
        assertEquals("None", curated.treatmentHistory());
        assertIntegerEquals(2014, curated.lastTreatmentYear());
        assertIntegerEquals(2, curated.lastTreatmentMonth());
        assertFalse(curated.isActive());
    }

    @Test
    public void shouldReadLesionLocationConfigs() {
        List<LesionLocationConfig> configs = database.lesionLocationConfigs();
        assertEquals(1, configs.size());

        LesionLocationConfig config = configs.get(0);
        assertEquals("Lever", config.input());
        assertEquals("Liver", config.location());
    }

    @Test
    public void shouldReadNonOncologicalHistoryConfigs() {
        List<NonOncologicalHistoryConfig> configs = database.nonOncologicalHistoryConfigs();
        assertEquals(4, configs.size());
        NonOncologicalHistoryConfig config1 = find(configs, "Levercirrose/ sarcoidose");
        assertFalse(config1.ignore());
        assertTrue(config1.priorOtherCondition().isPresent());
        assertFalse(config1.lvef().isPresent());

        PriorOtherCondition curated1 = config1.priorOtherCondition().get();
        assertEquals("Liver cirrhosis and sarcoidosis", curated1.name());
        assertIntegerEquals(2019, curated1.year());
        assertIntegerEquals(7, curated1.month());
        assertEquals("Liver disease", curated1.category());
        assertEquals(2, curated1.doids().size());
        assertTrue(curated1.doids().contains("5082"));
        assertTrue(curated1.doids().contains("11335"));
        assertFalse(curated1.isContraindicationForTherapy());

        NonOncologicalHistoryConfig config2 = find(configs, "NA");
        assertTrue(config2.ignore());
        assertFalse(config2.lvef().isPresent());
        assertFalse(config2.priorOtherCondition().isPresent());

        NonOncologicalHistoryConfig config3 = find(configs, "LVEF 0.17");
        assertFalse(config3.ignore());
        assertTrue(config3.lvef().isPresent());
        assertFalse(config3.priorOtherCondition().isPresent());
        assertEquals(0.17, config3.lvef().get(), EPSILON);

        NonOncologicalHistoryConfig config4 = find(configs, "No contraindication");
        assertTrue(config4.priorOtherCondition().isPresent());
        PriorOtherCondition curated4 = config4.priorOtherCondition().get();
        assertTrue(curated4.isContraindicationForTherapy());
    }

    @Test
    public void shouldReadECGConfigs() {
        List<ECGConfig> configs = database.ecgConfigs();
        assertEquals(4, configs.size());

        ECGConfig sinus = find(configs, "Sinus Tachycardia");
        assertEquals("Sinus tachycardia", sinus.interpretation());
        assertFalse(sinus.ignore());
        assertFalse(sinus.isQTCF());
        assertNull(sinus.qtcfValue());
        assertNull(sinus.qtcfUnit());
        assertFalse(sinus.isJTC());
        assertNull(sinus.jtcValue());
        assertNull(sinus.jtcUnit());

        ECGConfig qtcf = find(configs, "qtcf");
        assertTrue(qtcf.isQTCF());
        assertFalse(qtcf.ignore());
        assertIntegerEquals(470, qtcf.qtcfValue());
        assertEquals("ms", qtcf.qtcfUnit());

        ECGConfig jtc = find(configs, "jtc");
        assertTrue(jtc.isJTC());
        assertFalse(jtc.ignore());
        assertIntegerEquals(570, jtc.jtcValue());
        assertEquals("ms", jtc.jtcUnit());

        ECGConfig weird = find(configs, "weird");
        assertTrue(weird.ignore());
    }

    @Test
    public void shouldReadInfectionConfigs() {
        List<InfectionConfig> configs = database.infectionConfigs();
        assertEquals(2, configs.size());

        InfectionConfig config1 = find(configs, "YES lung abces");
        assertEquals("Lung abscess", config1.interpretation());

        InfectionConfig config2 = find(configs, "NA");
        assertEquals("No", config2.interpretation());
    }

    @Test
    public void shouldReadComplicationConfigs() {
        List<ComplicationConfig> configs = database.complicationConfigs();
        assertEquals(2, configs.size());

        ComplicationConfig config1 = find(configs, "something");
        assertFalse(config1.ignore());
        assertFalse(config1.impliesUnknownComplicationState());
        Complication curated1 = config1.curated();
        assertNotNull(curated1);
        assertEquals("curated something", curated1.name());
        assertEquals(2, curated1.categories().size());
        assertIntegerEquals(2000, curated1.year());
        assertIntegerEquals(1, curated1.month());

        ComplicationConfig config2 = find(configs, "unknown");
        assertFalse(config2.ignore());
        assertTrue(config2.impliesUnknownComplicationState());
        Complication curated2 = config2.curated();
        assertNotNull(curated2);
        assertEquals(Strings.EMPTY, curated2.name());
        assertEquals(0, curated2.categories().size());
        assertNull(curated2.year());
        assertNull(curated2.month());
    }

    @Test
    public void shouldReadToxicityConfigs() {
        List<ToxicityConfig> configs = database.toxicityConfigs();
        assertEquals(1, configs.size());

        ToxicityConfig config = configs.get(0);
        assertEquals("Neuropathy GR3", config.input());
        assertEquals("Neuropathy", config.name());
        assertEquals(Sets.newHashSet("Neuro"), config.categories());
        assertIntegerEquals(3, config.grade());
    }

    @Test
    public void shouldReadMolecularTestConfigs() {
        List<MolecularTestConfig> configs = database.molecularTestConfigs();
        assertEquals(1, configs.size());

        MolecularTestConfig config = configs.get(0);
        assertEquals("IHC ERBB2 3+", config.input());
        assertEquals(ImmutablePriorMolecularTest.builder()
                .test("IHC")
                .item("ERBB2")
                .measure(null)
                .scoreText(null)
                .scoreValuePrefix(null)
                .scoreValue(3D)
                .scoreValueUnit("+")
                .impliesPotentialIndeterminateStatus(false)
                .build(), config.curated());
    }

    @Test
    public void shouldReadMedicationNameConfigs() {
        List<MedicationNameConfig> configs = database.medicationNameConfigs();
        assertEquals(2, configs.size());

        MedicationNameConfig config1 = find(configs, "A en B");
        assertEquals("A and B", config1.name());
        assertFalse(config1.ignore());

        MedicationNameConfig config2 = find(configs, "No medication");
        assertTrue(config2.ignore());
    }

    @Test
    public void shouldReadMedicationDosageConfigs() {
        List<MedicationDosageConfig> configs = database.medicationDosageConfigs();
        assertEquals(2, configs.size());

        MedicationDosageConfig config1 = find(configs, "once per day 50-60 mg");
        assertDoubleEquals(50, config1.dosageMin());
        assertDoubleEquals(60, config1.dosageMax());
        assertEquals("mg", config1.dosageUnit());
        assertDoubleEquals(1, config1.frequency());
        assertEquals("day", config1.frequencyUnit());
        assertEquals(Boolean.FALSE, config1.ifNeeded());

        MedicationDosageConfig config2 = find(configs, "empty");
        assertNull(config2.dosageMin());
        assertNull(config2.dosageMax());
        assertNull(config2.dosageUnit());
        assertNull(config2.frequency());
        assertNull(config2.frequencyUnit());
        assertNull(config2.ifNeeded());
    }

    @Test
    public void shouldReadMedicationCategoryConfigs() {
        List<MedicationCategoryConfig> configs = database.medicationCategoryConfigs();
        assertEquals(2, configs.size());

        MedicationCategoryConfig paracetamol = find(configs, "Paracetamol");
        assertEquals(Sets.newHashSet("Acetanilide derivatives"), paracetamol.categories());

        MedicationCategoryConfig formoterol = find(configs, "Formoterol and budesonide");
        assertEquals(Sets.newHashSet("Beta2 sympathomimetics", "Corticosteroids"), formoterol.categories());
    }

    @Test
    public void shouldReadAllergyConfigs() {
        List<IntoleranceConfig> configs = database.intoleranceConfigs();
        assertEquals(1, configs.size());

        IntoleranceConfig config = find(configs, "Clindamycine");
        assertEquals("Clindamycin", config.name());
        assertEquals(Sets.newHashSet("0060500"), config.doids());
    }

    @NotNull
    private static <T extends CurationConfig> T find(@NotNull List<T> configs, @NotNull String input) {
        for (T config : configs) {
            if (config.input().equals(input)) {
                return config;
            }
        }

        throw new IllegalStateException("Could not find input '" + input + "' in configs");
    }

    @Test
    public void shouldReadAdministrationRouteTranslations() {
        List<AdministrationRouteTranslation> translations = database.administrationRouteTranslations();
        assertEquals(1, translations.size());

        AdministrationRouteTranslation translation = translations.get(0);
        assertEquals("ORAAL", translation.administrationRoute());
        assertEquals("Oral", translation.translatedAdministrationRoute());
    }

    @Test
    public void shouldReadLaboratoryTranslations() {
        List<LaboratoryTranslation> translations = database.laboratoryTranslations();
        assertEquals(1, translations.size());

        LaboratoryTranslation translation = translations.get(0);
        assertEquals("AC", translation.code());
        assertEquals("AC2", translation.translatedCode());
        assertEquals("ACTH", translation.name());
        assertEquals("Adrenocorticotropic hormone", translation.translatedName());
    }

    @Test
    public void shouldReadToxicityTranslations() {
        List<ToxicityTranslation> translations = database.toxicityTranslations();
        assertEquals(1, translations.size());

        ToxicityTranslation translation = translations.get(0);
        assertEquals("Pijn", translation.toxicity());
        assertEquals("Pain", translation.translatedToxicity());
    }

    @Test
    public void shouldReadBloodTransfusionTranslations() {
        List<BloodTransfusionTranslation> translations = database.bloodTransfusionTranslations();
        assertEquals(1, translations.size());

        BloodTransfusionTranslation translation = translations.get(0);
        assertEquals("Thrombocytenconcentraat", translation.product());
        assertEquals("Thrombocyte concentrate", translation.translatedProduct());
    }

    private void assertIntegerEquals(int expected, @Nullable Integer actual) {
        assertNotNull(actual);
        assertEquals(expected, (int) actual);
    }

    private void assertDoubleEquals(double expected, @Nullable Double actual) {
        assertNotNull(actual);
        assertEquals(expected, actual, EPSILON);
    }
}