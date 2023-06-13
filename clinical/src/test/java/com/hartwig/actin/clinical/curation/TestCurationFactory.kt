package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.ImmutableTreatmentHistoryEntryConfig

object TestCurationFactory {
    fun createProperTestCurationModel(): CurationModel {
        return CurationModel(createTestCurationDatabase(), questionnaireRawEntryMapper())
    }

    fun createMinimalTestCurationModel(): CurationModel {
        return CurationModel(ImmutableCurationDatabase.builder().build(), questionnaireRawEntryMapper())
    }

    fun createMinimalTestCurationDatabaseValidator(): CurationValidator {
        return CurationValidator(TestDoidModelFactory.createMinimalTestDoidModel())
    }

    fun createTestCurationDatabase(): CurationDatabase {
        return ImmutableCurationDatabase.builder()
            .primaryTumorConfigs(createTestPrimaryTumorConfigs())
            .treatmentHistoryEntryConfigs(createTestTreatmentHistoryEntryConfigs())
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
            .build()
    }

    private fun createTestPrimaryTumorConfigs(): List<PrimaryTumorConfig> {
        return java.util.List.of<PrimaryTumorConfig>(
            ImmutablePrimaryTumorConfig.builder()
                .input("Unknown | Carcinoma")
                .primaryTumorLocation("Unknown")
                .primaryTumorSubLocation("CUP")
                .primaryTumorType("Carcinoma")
                .primaryTumorSubType(org.apache.logging.log4j.util.Strings.EMPTY)
                .primaryTumorExtraDetails(org.apache.logging.log4j.util.Strings.EMPTY)
                .addDoids("299")
                .build()
        )
    }

    private fun createTestTreatmentHistoryEntryConfigs(): List<TreatmentHistoryEntryConfig> {
        val cisplatin: ImmutableChemotherapy =
            ImmutableChemotherapy.builder().name("Cisplatin").addCategories(TreatmentCategory.CHEMOTHERAPY).isSystemic(true).build()
        return java.util.List.of(
            ImmutableTreatmentHistoryEntryConfig.builder()
                .input("Cis 2020 2021")
                .ignore(false)
                .curated(ImmutableTreatmentHistoryEntry.builder().addTreatments(cisplatin).startYear(2020).build())
                .build(),
            ImmutableTreatmentHistoryEntryConfig.builder()
                .input("Cis 2020 2021")
                .ignore(false)
                .curated(ImmutableTreatmentHistoryEntry.builder().addTreatments(cisplatin).startYear(2021).build())
                .build(),
            ImmutableTreatmentHistoryEntryConfig.builder().input("no systemic treatment").ignore(true).build()
        )
    }

    private fun createTestOncologicalHistoryConfigs(): List<OncologicalHistoryConfig> {
        return java.util.List.of<OncologicalHistoryConfig>(
            ImmutableOncologicalHistoryConfig.builder()
                .input("Cis 2020 2021")
                .ignore(false)
                .curated(
                    ImmutablePriorTumorTreatment.builder()
                        .name("Cisplatin")
                        .startYear(2020)
                        .addCategories(TreatmentCategory.CHEMOTHERAPY)
                        .isSystemic(true)
                        .chemoType("platinum")
                        .build()
                )
                .build(),
            ImmutableOncologicalHistoryConfig.builder()
                .input("Cis 2020 2021")
                .ignore(false)
                .curated(
                    ImmutablePriorTumorTreatment.builder()
                        .name("Cisplatin")
                        .startYear(2021)
                        .addCategories(TreatmentCategory.CHEMOTHERAPY)
                        .isSystemic(true)
                        .chemoType("platinum")
                        .build()
                )
                .build(),
            ImmutableOncologicalHistoryConfig.builder().input("no systemic treatment").ignore(true).build()
        )
    }

    private fun createTestSecondPrimaryConfigs(): List<SecondPrimaryConfig> {
        return java.util.List.of<SecondPrimaryConfig>(
            ImmutableSecondPrimaryConfig.builder()
                .input("Breast cancer Jan-2018")
                .ignore(false)
                .curated(
                    ImmutablePriorSecondPrimary.builder()
                        .tumorLocation("Breast")
                        .tumorSubLocation(org.apache.logging.log4j.util.Strings.EMPTY)
                        .tumorType("Carcinoma")
                        .tumorSubType(org.apache.logging.log4j.util.Strings.EMPTY)
                        .diagnosedYear(2018)
                        .diagnosedMonth(1)
                        .treatmentHistory("Surgery")
                        .isActive(false)
                        .build()
                )
                .build()
        )
    }

    private fun createTestLesionLocationConfigs(): List<LesionLocationConfig> {
        return java.util.List.of<LesionLocationConfig>(
            ImmutableLesionLocationConfig.builder().input("Abdominal").location("Abdominal").build(),
            ImmutableLesionLocationConfig.builder().input("Lever").location("Liver").category(LesionLocationCategory.LIVER).build(),
            ImmutableLesionLocationConfig.builder().input("Cns").location("CNS").category(LesionLocationCategory.CNS).build(),
            ImmutableLesionLocationConfig.builder().input("Brain").location("Brain").category(LesionLocationCategory.BRAIN).build(),
            ImmutableLesionLocationConfig.builder().input("Bone").location("Bone").category(LesionLocationCategory.BONE).build(),
            ImmutableLesionLocationConfig.builder().input("Pulmonal").location("Lung").category(LesionLocationCategory.LUNG).build(),
            ImmutableLesionLocationConfig.builder()
                .input("Lymph node")
                .location("Lymph node")
                .category(LesionLocationCategory.LYMPH_NODE)
                .build(),
            ImmutableLesionLocationConfig.builder().input("Not a lesion").location(org.apache.logging.log4j.util.Strings.EMPTY).build(),
            ImmutableLesionLocationConfig.builder().input("No").location(org.apache.logging.log4j.util.Strings.EMPTY).build()
        )
    }

    private fun createTestNonOncologicalHistoryConfigs(): List<NonOncologicalHistoryConfig> {
        return java.util.List.of<NonOncologicalHistoryConfig>(
            TestCurationConfigFactory.nonOncologicalHistoryConfigBuilder()
                .input("sickness")
                .ignore(false)
                .priorOtherCondition(
                    java.util.Optional.of<ImmutablePriorOtherCondition>(
                        ImmutablePriorOtherCondition.builder()
                            .name("sick")
                            .category("being sick")
                            .isContraindicationForTherapy(true)
                            .build()
                    )
                )
                .build(),
            TestCurationConfigFactory.nonOncologicalHistoryConfigBuilder().input("not a condition").ignore(true).build(),
            TestCurationConfigFactory.nonOncologicalHistoryConfigBuilder()
                .input("LVEF 0.17")
                .ignore(false)
                .lvef(java.util.Optional.of(0.17))
                .build()
        )
    }

    private fun createTestECGConfigs(): List<ECGConfig> {
        return java.util.List.of<ECGConfig>(
            ImmutableECGConfig.builder()
                .input("Weird aberration")
                .ignore(false)
                .interpretation("Cleaned aberration")
                .isQTCF(false)
                .isJTC(false)
                .build(),
            ImmutableECGConfig.builder()
                .input("No aberration")
                .ignore(true)
                .interpretation(org.apache.logging.log4j.util.Strings.EMPTY)
                .isQTCF(false)
                .isJTC(false)
                .build(),
            ImmutableECGConfig.builder()
                .input("Yes but unknown what aberration")
                .ignore(false)
                .interpretation(org.apache.logging.log4j.util.Strings.EMPTY)
                .isQTCF(false)
                .isJTC(false)
                .build()
        )
    }

    private fun createTestInfectionConfigs(): List<InfectionConfig> {
        return java.util.List.of<InfectionConfig>(
            ImmutableInfectionConfig.builder().input("Weird infection").interpretation("Cleaned infection").build(),
            ImmutableInfectionConfig.builder().input("No infection").interpretation(org.apache.logging.log4j.util.Strings.EMPTY).build()
        )
    }

    private fun createTestComplicationConfigs(): List<ComplicationConfig> {
        return java.util.List.of<ComplicationConfig>(
            ImmutableComplicationConfig.builder()
                .input("Term")
                .ignore(false)
                .impliesUnknownComplicationState(false)
                .curated(ImmutableComplication.builder().name("Curated").addCategories("Curated category").build())
                .build(),
            ImmutableComplicationConfig.builder()
                .input("Unknown")
                .ignore(false)
                .impliesUnknownComplicationState(true)
                .curated(ImmutableComplication.builder().name(org.apache.logging.log4j.util.Strings.EMPTY).build())
                .build(),
            ImmutableComplicationConfig.builder()
                .input("None")
                .ignore(true)
                .impliesUnknownComplicationState(false)
                .curated(null)
                .build(),
            ImmutableComplicationConfig.builder()
                .input("vomit")
                .ignore(false)
                .impliesUnknownComplicationState(false)
                .curated(ImmutableComplication.builder().name("Vomit").addCategories("Vomit category").build())
                .build()
        )
    }

    private fun createTestToxicityConfigs(): List<ToxicityConfig> {
        return java.util.List.of<ToxicityConfig>(
            ImmutableToxicityConfig.builder()
                .ignore(false)
                .input("neuropathy gr3")
                .name("neuropathy")
                .addCategories("neuro")
                .grade(3)
                .build()
        )
    }

    private fun createTestMolecularTestConfigs(): List<MolecularTestConfig> {
        return java.util.List.of<MolecularTestConfig>(
            ImmutableMolecularTestConfig.builder()
                .input("IHC ERBB2 3+")
                .ignore(false)
                .curated(
                    ImmutablePriorMolecularTest.builder()
                        .test("IHC")
                        .item("ERBB2")
                        .measure(null)
                        .scoreText(null)
                        .scoreValuePrefix(null)
                        .scoreValue(3.0)
                        .scoreValueUnit("+")
                        .impliesPotentialIndeterminateStatus(false)
                        .build()
                )
                .build()
        )
    }

    private fun createTestMedicationNameConfigs(): List<MedicationNameConfig> {
        return java.util.List.of<MedicationNameConfig>(
            ImmutableMedicationNameConfig.builder().input("A en B").ignore(false).name("A and B").build(),
            ImmutableMedicationNameConfig.builder().input("No medication").ignore(true).name(org.apache.logging.log4j.util.Strings.EMPTY)
                .build()
        )
    }

    private fun createTestMedicationDosageConfigs(): List<MedicationDosageConfig> {
        return java.util.List.of<MedicationDosageConfig>(
            ImmutableMedicationDosageConfig.builder()
                .input("50-60 mg per day")
                .dosageMin(50.0)
                .dosageMax(60.0)
                .dosageUnit("mg")
                .frequency(1.0)
                .frequencyUnit("day")
                .ifNeeded(false)
                .build()
        )
    }

    private fun createTestMedicationCategoryConfigs(): List<MedicationCategoryConfig> {
        return java.util.List.of<MedicationCategoryConfig>(
            ImmutableMedicationCategoryConfig.builder().input("Paracetamol").addCategories("Acetanilide derivatives").build()
        )
    }

    private fun createTestIntoleranceConfigs(): List<IntoleranceConfig> {
        return java.util.List.of<IntoleranceConfig>(
            ImmutableIntoleranceConfig.builder().input("Latex type 1").name("Latex (type 1)").addDoids("0060532").build()
        )
    }

    private fun createTestAdministrationRouteTranslations(): List<AdministrationRouteTranslation> {
        return java.util.List.of<AdministrationRouteTranslation>(
            ImmutableAdministrationRouteTranslation.builder()
                .administrationRoute("ignore")
                .translatedAdministrationRoute(org.apache.logging.log4j.util.Strings.EMPTY)
                .build(),
            ImmutableAdministrationRouteTranslation.builder()
                .administrationRoute("oraal")
                .translatedAdministrationRoute("oral")
                .build()
        )
    }

    private fun createTestLaboratoryTranslations(): List<LaboratoryTranslation> {
        return java.util.List.of<LaboratoryTranslation>(
            ImmutableLaboratoryTranslation.builder()
                .code("CO")
                .translatedCode("CODE")
                .name("naam")
                .translatedName("Name")
                .build()
        )
    }

    private fun createTestToxicityTranslations(): List<ToxicityTranslation> {
        return java.util.List.of<ToxicityTranslation>(
            ImmutableToxicityTranslation.builder().toxicity("Pijn").translatedToxicity("Pain").build()
        )
    }

    private fun createTestBloodTransfusionTranslations(): List<BloodTransfusionTranslation> {
        return java.util.List.of<BloodTransfusionTranslation>(
            ImmutableBloodTransfusionTranslation.builder().product("Product").translatedProduct("Translated product").build(),
            ImmutableBloodTransfusionTranslation.builder().product("Not used").translatedProduct("never used").build()
        )
    }

    private fun questionnaireRawEntryMapper(): QuestionnaireRawEntryMapper {
        return QuestionnaireRawEntryMapper(emptyMap<String, String>())
    }
}