package com.hartwig.actin.clinical.curation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.curation.config.CancerRelatedComplicationConfig;
import com.hartwig.actin.clinical.curation.config.ECGConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableCancerRelatedComplicationConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableECGConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableInfectionConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableLesionLocationConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableMedicationCategoryConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableMedicationDosageConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableNonOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.ImmutablePrimaryTumorConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableToxicityConfig;
import com.hartwig.actin.clinical.curation.config.InfectionConfig;
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig;
import com.hartwig.actin.clinical.curation.config.MedicationCategoryConfig;
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig;
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig;
import com.hartwig.actin.clinical.curation.config.ToxicityConfig;
import com.hartwig.actin.clinical.curation.translation.AllergyTranslation;
import com.hartwig.actin.clinical.curation.translation.BloodTransfusionTranslation;
import com.hartwig.actin.clinical.curation.translation.ImmutableAllergyTranslation;
import com.hartwig.actin.clinical.curation.translation.ImmutableBloodTransfusionTranslation;
import com.hartwig.actin.clinical.curation.translation.ImmutableLaboratoryTranslation;
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation;
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
                .lesionLocationConfigs(createTestLesionLocationConfigs())
                .oncologicalHistoryConfigs(createTestOncologicalHistoryConfigs())
                .nonOncologicalHistoryConfigs(createTestNonOncologicalHistoryConfigs())
                .ecgConfigs(createTestECGConfigs())
                .infectionConfigs(createTestInfectionConfigs())
                .cancerRelatedComplicationConfigs(createTestCancerRelatedComplicationConfigs())
                .toxicityConfigs(createTestToxicityConfigs())
                .medicationDosageConfigs(createTestMedicationDosageConfigs())
                .medicationCategoryConfigs(createTestMedicationCategoryConfigs())
                .laboratoryTranslations(createTestLaboratoryTranslations())
                .allergyTranslations(createTestAllergyTranslations())
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
    private static List<LesionLocationConfig> createTestLesionLocationConfigs() {
        List<LesionLocationConfig> configs = Lists.newArrayList();

        configs.add(ImmutableLesionLocationConfig.builder().input("Lever").ignoreWhenOtherLesion(false).location("Liver").build());

        configs.add(ImmutableLesionLocationConfig.builder()
                .input("Not a lesion")
                .ignoreWhenOtherLesion(true)
                .location(Strings.EMPTY)
                .build());

        configs.add(ImmutableLesionLocationConfig.builder().input("No").ignoreWhenOtherLesion(false).location(Strings.EMPTY).build());

        return configs;
    }

    @NotNull
    private static List<OncologicalHistoryConfig> createTestOncologicalHistoryConfigs() {
        List<OncologicalHistoryConfig> configs = Lists.newArrayList();

        configs.add(ImmutableOncologicalHistoryConfig.builder()
                .input("Cis 2020")
                .ignore(false)
                .curatedObject(ImmutablePriorTumorTreatment.builder()
                        .name("Cisplatin")
                        .year(2020)
                        .addCategories(TreatmentCategory.CHEMOTHERAPY)
                        .isSystemic(true)
                        .chemoType("platinum")
                        .build())
                .build());

        configs.add(ImmutableOncologicalHistoryConfig.builder()
                .input("Breast cancer Jan-2018")
                .ignore(false)
                .curatedObject(ImmutablePriorSecondPrimary.builder()
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

        configs.add(ImmutableOncologicalHistoryConfig.builder().input("no systemic treatment").ignore(true).build());

        return configs;
    }

    @NotNull
    private static List<NonOncologicalHistoryConfig> createTestNonOncologicalHistoryConfigs() {
        List<NonOncologicalHistoryConfig> configs = Lists.newArrayList();

        configs.add(ImmutableNonOncologicalHistoryConfig.builder()
                .input("sickness")
                .ignore(false)
                .curated(ImmutablePriorOtherCondition.builder().name("sick").category("being sick").build())
                .build());

        configs.add(ImmutableNonOncologicalHistoryConfig.builder().input("not a condition").ignore(true).build());

        configs.add(ImmutableNonOncologicalHistoryConfig.builder().input("LVEF 0.17").ignore(false).curated(0.17).build());

        return configs;
    }

    @NotNull
    private static List<ECGConfig> createTestECGConfigs() {
        List<ECGConfig> configs = Lists.newArrayList();

        configs.add(ImmutableECGConfig.builder().input("Weird aberration").interpretation("Cleaned aberration").build());
        configs.add(ImmutableECGConfig.builder().input("No aberration").interpretation("NULL").build());

        return configs;
    }

    @NotNull
    private static List<InfectionConfig> createTestInfectionConfigs() {
        List<InfectionConfig> configs = Lists.newArrayList();

        configs.add(ImmutableInfectionConfig.builder().input("Weird infection").interpretation("Cleaned infection").build());
        configs.add(ImmutableInfectionConfig.builder().input("No infection").interpretation("NULL").build());

        return configs;
    }

    @NotNull
    private static List<CancerRelatedComplicationConfig> createTestCancerRelatedComplicationConfigs() {
        List<CancerRelatedComplicationConfig> configs = Lists.newArrayList();

        configs.add(ImmutableCancerRelatedComplicationConfig.builder().input("term").name("curated").build());

        return configs;
    }

    @NotNull
    private static List<ToxicityConfig> createTestToxicityConfigs() {
        List<ToxicityConfig> configs = Lists.newArrayList();

        configs.add(ImmutableToxicityConfig.builder().ignore(false).input("neuropathy gr3").name("neuropathy").grade(3).build());

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
    private static List<AllergyTranslation> createTestAllergyTranslations() {
        List<AllergyTranslation> translations = Lists.newArrayList();

        translations.add(ImmutableAllergyTranslation.builder().name("Naam").translatedName("Name").build());
        translations.add(ImmutableAllergyTranslation.builder().name("Not used").translatedName("never used").build());

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
