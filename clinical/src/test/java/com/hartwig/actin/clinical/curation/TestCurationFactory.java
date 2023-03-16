package com.hartwig.actin.clinical.curation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.curation.config.ComplicationConfig;
import com.hartwig.actin.clinical.curation.config.ECGConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableComplicationConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableECGConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableInfectionConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableIntoleranceConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableLesionLocationConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableMedicationCategoryConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableMedicationDosageConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableMedicationNameConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableMolecularTestConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableNonOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.ImmutablePrimaryTumorConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableSecondPrimaryConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableToxicityConfig;
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
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory;
import com.hartwig.actin.clinical.curation.translation.AdministrationRouteTranslation;
import com.hartwig.actin.clinical.curation.translation.BloodTransfusionTranslation;
import com.hartwig.actin.clinical.curation.translation.ImmutableAdministrationRouteTranslation;
import com.hartwig.actin.clinical.curation.translation.ImmutableBloodTransfusionTranslation;
import com.hartwig.actin.clinical.curation.translation.ImmutableLaboratoryTranslation;
import com.hartwig.actin.clinical.curation.translation.ImmutableToxicityTranslation;
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation;
import com.hartwig.actin.clinical.curation.translation.ToxicityTranslation;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestCurationFactory {

    private TestCurationFactory() {
    }

    @NotNull
    public static CurationModel createProperTestCurationModel() {
        return new CurationModel(createTestCurationDatabase());
    }

    @NotNull
    public static CurationModel createMinimalTestCurationModel() {
        return new CurationModel(ImmutableCurationDatabase.builder().build());
    }

    @NotNull
    private static CurationDatabase createTestCurationDatabase() {
        return ImmutableCurationDatabase.builder()
                .primaryTumorConfigs(createTestPrimaryTumorConfigs())
                .oncologicalHistoryConfigs(createTestOncologicalHistoryConfigs())
                .secondPrimaryConfigs(createTestSecondPrimaryConfigs())
                .lesionLocationConfigs(createTestLesionLocationConfigs())
                .nonOncologicalHistoryConfigs(createTestNonOncologicalHistoryConfigs())
                .ecgConfigs(createTestECGConfigs())
                .infectionConfigs(createTestInfectionConfigs())
                .complicationConfigs(createTestComplicationConfigs())
                .toxicityConfigs(createTestToxicityConfigs())
                .molecularTestConfigs(createTestMolecularTestConfigs())
                .medicationNameConfigs(createTestMedicationNameConfigs())
                .medicationDosageConfigs(createTestMedicationDosageConfigs())
                .medicationCategoryConfigs(createTestMedicationCategoryConfigs())
                .intoleranceConfigs(createTestIntoleranceConfigs())
                .administrationRouteTranslations(createTestAdministrationRouteTranslations())
                .laboratoryTranslations(createTestLaboratoryTranslations())
                .toxicityTranslations(createTestToxicityTranslations())
                .bloodTransfusionTranslations(createTestBloodTransfusionTranslations())
                .build();
    }

    @NotNull
    private static List<PrimaryTumorConfig> createTestPrimaryTumorConfigs() {
        List<PrimaryTumorConfig> configs = Lists.newArrayList();

        configs.add(ImmutablePrimaryTumorConfig.builder()
                .input("Unknown | Carcinoma")
                .primaryTumorLocation("Unknown")
                .primaryTumorSubLocation("CUP")
                .primaryTumorType("Carcinoma")
                .primaryTumorSubType(Strings.EMPTY)
                .primaryTumorExtraDetails(Strings.EMPTY)
                .addDoids("299")
                .build());

        return configs;
    }

    @NotNull
    private static List<OncologicalHistoryConfig> createTestOncologicalHistoryConfigs() {
        List<OncologicalHistoryConfig> configs = Lists.newArrayList();

        configs.add(ImmutableOncologicalHistoryConfig.builder()
                .input("Cis 2020 2021")
                .ignore(false)
                .curated(ImmutablePriorTumorTreatment.builder()
                        .name("Cisplatin")
                        .startYear(2020)
                        .addCategories(TreatmentCategory.CHEMOTHERAPY)
                        .isSystemic(true)
                        .chemoType("platinum")
                        .build())
                .build());

        configs.add(ImmutableOncologicalHistoryConfig.builder()
                .input("Cis 2020 2021")
                .ignore(false)
                .curated(ImmutablePriorTumorTreatment.builder()
                        .name("Cisplatin")
                        .startYear(2021)
                        .addCategories(TreatmentCategory.CHEMOTHERAPY)
                        .isSystemic(true)
                        .chemoType("platinum")
                        .build())
                .build());

        configs.add(ImmutableOncologicalHistoryConfig.builder().input("no systemic treatment").ignore(true).build());

        return configs;
    }

    @NotNull
    private static List<SecondPrimaryConfig> createTestSecondPrimaryConfigs() {
        List<SecondPrimaryConfig> configs = Lists.newArrayList();

        configs.add(ImmutableSecondPrimaryConfig.builder()
                .input("Breast cancer Jan-2018")
                .ignore(false)
                .curated(ImmutablePriorSecondPrimary.builder()
                        .tumorLocation("Breast")
                        .tumorSubLocation(Strings.EMPTY)
                        .tumorType("Carcinoma")
                        .tumorSubType(Strings.EMPTY)
                        .diagnosedYear(2018)
                        .diagnosedMonth(1)
                        .treatmentHistory("Surgery")
                        .isActive(false)
                        .build())
                .build());

        return configs;
    }

    @NotNull
    private static List<LesionLocationConfig> createTestLesionLocationConfigs() {
        List<LesionLocationConfig> configs = Lists.newArrayList();

        configs.add(ImmutableLesionLocationConfig.builder().input("Abdominal").location("Abdominal").build());

        configs.add(ImmutableLesionLocationConfig.builder()
                .input("Lever")
                .location("Liver")
                .category(LesionLocationCategory.LIVER)
                .build());

        configs.add(ImmutableLesionLocationConfig.builder().input("Cns").location("CNS").category(LesionLocationCategory.CNS).build());

        configs.add(ImmutableLesionLocationConfig.builder()
                .input("Brain")
                .location("Brain")
                .category(LesionLocationCategory.BRAIN)
                .build());

        configs.add(ImmutableLesionLocationConfig.builder().input("Bone").location("Bone").category(LesionLocationCategory.BONE).build());

        configs.add(ImmutableLesionLocationConfig.builder()
                .input("Pulmonal")
                .location("Lung")
                .category(LesionLocationCategory.LUNG)
                .build());

        configs.add(ImmutableLesionLocationConfig.builder().input("Lymph node").location("Lymph node")
                .category(LesionLocationCategory.LYMPH_NODE).build());

        configs.add(ImmutableLesionLocationConfig.builder().input("Not a lesion").location(Strings.EMPTY).build());

        configs.add(ImmutableLesionLocationConfig.builder().input("No").location(Strings.EMPTY).build());

        return configs;
    }

    @NotNull
    private static List<NonOncologicalHistoryConfig> createTestNonOncologicalHistoryConfigs() {
        List<NonOncologicalHistoryConfig> configs = Lists.newArrayList();

        configs.add(ImmutableNonOncologicalHistoryConfig.builder()
                .input("sickness")
                .ignore(false)
                .curated(ImmutablePriorOtherCondition.builder()
                        .name("sick")
                        .category("being sick")
                        .isContraindicationForTherapy(true)
                        .build())
                .build());

        configs.add(ImmutableNonOncologicalHistoryConfig.builder().input("not a condition").ignore(true).build());

        configs.add(ImmutableNonOncologicalHistoryConfig.builder().input("LVEF 0.17").ignore(false).curated(0.17).build());

        return configs;
    }

    @NotNull
    private static List<ECGConfig> createTestECGConfigs() {
        List<ECGConfig> configs = Lists.newArrayList();

        configs.add(ImmutableECGConfig.builder()
                .input("Weird aberration")
                .ignore(false)
                .interpretation("Cleaned aberration")
                .isQTCF(false)
                .isJTC(false)
                .build());

        configs.add(ImmutableECGConfig.builder()
                .input("No aberration")
                .ignore(true)
                .interpretation(Strings.EMPTY)
                .isQTCF(false)
                .isJTC(false)
                .build());

        configs.add(ImmutableECGConfig.builder()
                .input("Yes but unknown what aberration")
                .ignore(false)
                .interpretation(Strings.EMPTY)
                .isQTCF(false)
                .isJTC(false)
                .build());

        return configs;
    }

    @NotNull
    private static List<InfectionConfig> createTestInfectionConfigs() {
        List<InfectionConfig> configs = Lists.newArrayList();

        configs.add(ImmutableInfectionConfig.builder().input("Weird infection").interpretation("Cleaned infection").build());
        configs.add(ImmutableInfectionConfig.builder().input("No infection").interpretation(Strings.EMPTY).build());

        return configs;
    }

    @NotNull
    private static List<ComplicationConfig> createTestComplicationConfigs() {
        List<ComplicationConfig> configs = Lists.newArrayList();

        configs.add(ImmutableComplicationConfig.builder()
                .input("Term")
                .ignore(false)
                .impliesUnknownComplicationState(false)
                .curated(ImmutableComplication.builder().name("Curated").addCategories("Curated category").build())
                .build());

        configs.add(ImmutableComplicationConfig.builder()
                .input("Unknown")
                .ignore(false)
                .impliesUnknownComplicationState(true)
                .curated(ImmutableComplication.builder().name(Strings.EMPTY).build())
                .build());

        configs.add(ImmutableComplicationConfig.builder()
                .input("None")
                .ignore(true)
                .impliesUnknownComplicationState(false)
                .curated(null)
                .build());

        return configs;
    }

    @NotNull
    private static List<ToxicityConfig> createTestToxicityConfigs() {
        List<ToxicityConfig> configs = Lists.newArrayList();

        configs.add(ImmutableToxicityConfig.builder()
                .ignore(false)
                .input("neuropathy gr3")
                .name("neuropathy")
                .addCategories("neuro")
                .grade(3)
                .build());

        return configs;
    }

    @NotNull
    private static List<MolecularTestConfig> createTestMolecularTestConfigs() {
        List<MolecularTestConfig> configs = Lists.newArrayList();

        configs.add(ImmutableMolecularTestConfig.builder()
                .input("IHC ERBB2 3+")
                .ignore(false)
                .curated(ImmutablePriorMolecularTest.builder()
                        .test("IHC")
                        .item("ERBB2")
                        .measure(null)
                        .scoreText(null)
                        .scoreValuePrefix(null)
                        .scoreValue(3D)
                        .scoreValueUnit("+")
                        .impliesPotentialIndeterminateStatus(false)
                        .build())
                .build());

        return configs;
    }

    @NotNull
    private static List<MedicationNameConfig> createTestMedicationNameConfigs() {
        List<MedicationNameConfig> configs = Lists.newArrayList();

        configs.add(ImmutableMedicationNameConfig.builder().input("A en B").ignore(false).name("A and B").build());
        configs.add(ImmutableMedicationNameConfig.builder().input("No medication").ignore(true).name(Strings.EMPTY).build());

        return configs;
    }

    @NotNull
    private static List<MedicationDosageConfig> createTestMedicationDosageConfigs() {
        List<MedicationDosageConfig> configs = Lists.newArrayList();

        configs.add(ImmutableMedicationDosageConfig.builder()
                .input("50-60 mg per day")
                .dosageMin(50D)
                .dosageMax(60D)
                .dosageUnit("mg")
                .frequency(1D)
                .frequencyUnit("day")
                .ifNeeded(false)
                .build());

        return configs;
    }

    @NotNull
    private static List<MedicationCategoryConfig> createTestMedicationCategoryConfigs() {
        List<MedicationCategoryConfig> configs = Lists.newArrayList();

        configs.add(ImmutableMedicationCategoryConfig.builder().input("Paracetamol").addCategories("Acetanilide derivatives").build());

        return configs;
    }

    @NotNull
    private static List<IntoleranceConfig> createTestIntoleranceConfigs() {
        List<IntoleranceConfig> configs = Lists.newArrayList();

        configs.add(ImmutableIntoleranceConfig.builder().input("Latex type 1").name("Latex (type 1)").addDoids("0060532").build());

        return configs;
    }

    @NotNull
    private static List<AdministrationRouteTranslation> createTestAdministrationRouteTranslations() {
        List<AdministrationRouteTranslation> configs = Lists.newArrayList();

        configs.add(ImmutableAdministrationRouteTranslation.builder()
                .administrationRoute("ignore")
                .translatedAdministrationRoute(Strings.EMPTY)
                .build());

        configs.add(ImmutableAdministrationRouteTranslation.builder()
                .administrationRoute("oraal")
                .translatedAdministrationRoute("oral")
                .build());

        return configs;
    }

    @NotNull
    private static List<LaboratoryTranslation> createTestLaboratoryTranslations() {
        List<LaboratoryTranslation> translations = Lists.newArrayList();

        translations.add(ImmutableLaboratoryTranslation.builder()
                .code("CO")
                .translatedCode("CODE")
                .name("naam")
                .translatedName("Name")
                .build());

        return translations;
    }

    @NotNull
    private static List<ToxicityTranslation> createTestToxicityTranslations() {
        List<ToxicityTranslation> translations = Lists.newArrayList();

        translations.add(ImmutableToxicityTranslation.builder().toxicity("Pijn").translatedToxicity("Pain").build());

        return translations;
    }

    @NotNull
    private static List<BloodTransfusionTranslation> createTestBloodTransfusionTranslations() {
        List<BloodTransfusionTranslation> translations = Lists.newArrayList();

        translations.add(ImmutableBloodTransfusionTranslation.builder().product("Product").translatedProduct("Translated product").build());
        translations.add(ImmutableBloodTransfusionTranslation.builder().product("Not used").translatedProduct("never used").build());

        return translations;
    }
}
