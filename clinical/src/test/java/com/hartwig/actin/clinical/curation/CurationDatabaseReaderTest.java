package com.hartwig.actin.clinical.curation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.hartwig.actin.clinical.curation.config.AllergyConfig;
import com.hartwig.actin.clinical.curation.config.ComplicationConfig;
import com.hartwig.actin.clinical.curation.config.CurationConfig;
import com.hartwig.actin.clinical.curation.config.ECGConfig;
import com.hartwig.actin.clinical.curation.config.InfectionConfig;
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig;
import com.hartwig.actin.clinical.curation.config.MedicationCategoryConfig;
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig;
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig;
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig;
import com.hartwig.actin.clinical.curation.config.ToxicityConfig;
import com.hartwig.actin.clinical.curation.translation.BloodTransfusionTranslation;
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class CurationDatabaseReaderTest {

    private static final double EPSILON = 1.0E-10;

    private static final String CURATION_DIRECTORY = Resources.getResource("curation").getPath();

    @Test
    public void canReadFromTestDirectory() throws IOException {
        CurationDatabase database = CurationDatabaseReader.read(CURATION_DIRECTORY);

        assertPrimaryTumorConfigs(database.primaryTumorConfigs());
        assertLesionLocationConfigs(database.lesionLocationConfigs());
        assertOncologicalHistoryConfigs(database.oncologicalHistoryConfigs());
        assertNonOncologicalHistoryConfigs(database.nonOncologicalHistoryConfigs());
        assertECGConfigs(database.ecgConfigs());
        assertInfectionConfigs(database.infectionConfigs());
        assertComplicationConfigs(database.complicationConfigs());
        assertToxicityConfigs(database.toxicityConfigs());
        assertMolecularTestConfigs(database.molecularTestConfigs());
        assertMedicationDosageConfigs(database.medicationDosageConfigs());
        assertMedicationCategoryConfigs(database.medicationCategoryConfigs());
        assertAllergyConfigs(database.allergyConfigs());

        assertLaboratoryTranslations(database.laboratoryTranslations());
        assertBloodTransfusionTranslations(database.bloodTransfusionTranslations());
    }

    private static void assertPrimaryTumorConfigs(@NotNull List<PrimaryTumorConfig> configs) {
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

    private static void assertLesionLocationConfigs(@NotNull List<LesionLocationConfig> configs) {
        assertEquals(1, configs.size());

        LesionLocationConfig config = configs.get(0);
        assertEquals("Lever", config.input());
        assertEquals("Liver", config.location());
    }

    private static void assertOncologicalHistoryConfigs(@NotNull List<OncologicalHistoryConfig> configs) {
        assertEquals(2, configs.size());

        OncologicalHistoryConfig config1 = find(configs, "Capecitabine/Oxaliplatin 2020");
        assertFalse(config1.ignore());

        PriorTumorTreatment curated1 = (PriorTumorTreatment) config1.curated();
        assertEquals("Capecitabine+Oxaliplatin", curated1.name());
        assertEquals(2020, (int) curated1.year());
        assertNull(curated1.month());
        assertEquals(Sets.newHashSet(TreatmentCategory.CHEMOTHERAPY), curated1.categories());
        assertTrue(curated1.isSystemic());
        assertEquals("antimetabolite,platinum", curated1.chemoType());
        assertNull(curated1.immunoType());
        assertNull(curated1.targetedType());
        assertNull(curated1.hormoneType());
        assertNull(curated1.radioType());
        assertNull(curated1.transplantType());
        assertNull(curated1.supportiveType());
        assertNull(curated1.trialAcronym());

        OncologicalHistoryConfig config2 = find(configs, "Breast Jan-2018");
        assertFalse(config2.ignore());

        PriorSecondPrimary curated2 = (PriorSecondPrimary) config2.curated();
        assertEquals("Breast", curated2.tumorLocation());
        assertEquals(Strings.EMPTY, curated2.tumorSubLocation());
        assertEquals("Carcinoma", curated2.tumorType());
        assertEquals(Strings.EMPTY, curated2.tumorSubType());
        assertTrue(curated2.doids().isEmpty());
        assertEquals(2018, (int) curated2.diagnosedYear());
        assertEquals(1, (int) curated2.diagnosedMonth());
        assertEquals("Surgery", curated2.treatmentHistory());
        assertEquals(2019, (int) curated2.lastTreatmentYear());
        assertEquals(2, (int) curated2.lastTreatmentMonth());
        assertTrue(curated2.isActive());
    }

    private static void assertNonOncologicalHistoryConfigs(@NotNull List<NonOncologicalHistoryConfig> configs) {
        assertEquals(3, configs.size());
        NonOncologicalHistoryConfig config1 = find(configs, "Levercirrose/ sarcoidose");
        assertFalse(config1.ignore());

        PriorOtherCondition curated1 = (PriorOtherCondition) config1.curated();
        assertEquals("Liver cirrhosis and sarcoidosis", curated1.name());
        assertEquals(2019, (int) curated1.year());
        assertEquals("Liver disease", curated1.category());
        assertEquals(2, curated1.doids().size());
        assertTrue(curated1.doids().contains("5082"));
        assertTrue(curated1.doids().contains("11335"));

        NonOncologicalHistoryConfig config2 = find(configs, "NA");
        assertTrue(config2.ignore());
        assertNull(config2.curated());

        NonOncologicalHistoryConfig config3 = find(configs, "LVEF 0.17");
        assertFalse(config3.ignore());
        assertEquals(0.17, (Double) config3.curated(), EPSILON);
    }

    private static void assertECGConfigs(@NotNull List<ECGConfig> configs) {
        assertEquals(3, configs.size());

        ECGConfig sinus = find(configs, "Sinus Tachycardia");
        assertEquals("Sinus tachycardia", sinus.interpretation());
        assertFalse(sinus.ignore());
        assertFalse(sinus.isQTCF());
        assertNull(sinus.qtcfValue());
        assertNull(sinus.qtcfUnit());

        ECGConfig qtcf = find(configs, "qtcf");
        assertTrue(qtcf.isQTCF());
        assertFalse(qtcf.ignore());
        assertEquals(470, (int) qtcf.qtcfValue());
        assertEquals("ms", qtcf.qtcfUnit());

        ECGConfig weird = find(configs, "weird");
        assertTrue(weird.ignore());
    }

    private static void assertInfectionConfigs(@NotNull List<InfectionConfig> configs) {
        assertEquals(2, configs.size());

        InfectionConfig config1 = find(configs, "YES lung abces");
        assertEquals("Lung abscess", config1.interpretation());

        InfectionConfig config2 = find(configs, "NA");
        assertEquals("No", config2.interpretation());
    }

    private static void assertComplicationConfigs(@NotNull List<ComplicationConfig> configs) {
        assertEquals(1, configs.size());

        ComplicationConfig config = configs.get(0);
        assertEquals("something", config.input());
        assertEquals("curated something", config.curated().name());
        assertEquals(2000, (int) config.curated().year());
        assertEquals(1, (int) config.curated().month());
    }

    private static void assertToxicityConfigs(@NotNull List<ToxicityConfig> configs) {
        assertEquals(1, configs.size());

        ToxicityConfig config = configs.get(0);
        assertEquals("Neuropathy GR3", config.input());
        assertEquals("Neuropathy", config.name());
        assertEquals(3, (int) config.grade());
    }

    private static void assertMolecularTestConfigs(@NotNull List<MolecularTestConfig> configs) {
        assertEquals(1, configs.size());

        MolecularTestConfig config = configs.get(0);
        assertEquals("IHC ERBB2 3+", config.input());
        assertEquals(ImmutablePriorMolecularTest.builder()
                .test("IHC")
                .item("ERBB2")
                .measure(null)
                .scoreText(null)
                .scoreValue(3D)
                .unit("+")
                .build(), config.curated());
    }

    private static void assertMedicationDosageConfigs(@NotNull List<MedicationDosageConfig> configs) {
        assertEquals(2, configs.size());

        MedicationDosageConfig config1 = find(configs, "once per day 50-60 mg");
        assertEquals(50, config1.dosageMin(), EPSILON);
        assertEquals(60, config1.dosageMax(), EPSILON);
        assertEquals("mg", config1.dosageUnit());
        assertEquals(1, config1.frequency(), EPSILON);
        assertEquals("day", config1.frequencyUnit());
        assertFalse(config1.ifNeeded());

        MedicationDosageConfig config2 = find(configs, "empty");
        assertNull(config2.dosageMin());
        assertNull(config2.dosageMax());
        assertNull(config2.dosageUnit());
        assertNull(config2.frequency());
        assertNull(config2.frequencyUnit());
        assertNull(config2.ifNeeded());
    }

    private static void assertMedicationCategoryConfigs(@NotNull List<MedicationCategoryConfig> configs) {
        assertEquals(2, configs.size());

        MedicationCategoryConfig paracetamol = find(configs, "Paracetamol");
        assertEquals(Sets.newHashSet("Acetanilide derivatives"), paracetamol.categories());

        MedicationCategoryConfig formoterol = find(configs, "Formoterol and budesonide");
        assertEquals(Sets.newHashSet("Beta2 sympathomimetics", "Corticosteroids"), formoterol.categories());
    }

    private static void assertAllergyConfigs(@NotNull List<AllergyConfig> configs) {
        assertEquals(1, configs.size());

        AllergyConfig config = find(configs, "Clindamycine");
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

    private static void assertLaboratoryTranslations(@NotNull List<LaboratoryTranslation> translations) {
        assertEquals(1, translations.size());

        LaboratoryTranslation translation = translations.get(0);
        assertEquals("AC", translation.code());
        assertEquals("AC2", translation.translatedCode());
        assertEquals("ACTH", translation.name());
        assertEquals("Adrenocorticotropic hormone", translation.translatedName());
    }

    private static void assertBloodTransfusionTranslations(@NotNull List<BloodTransfusionTranslation> translations) {
        assertEquals(1, translations.size());

        BloodTransfusionTranslation translation = translations.get(0);
        assertEquals("Thrombocytenconcentraat", translation.product());
        assertEquals("Thrombocyte concentrate", translation.translatedProduct());
    }
}