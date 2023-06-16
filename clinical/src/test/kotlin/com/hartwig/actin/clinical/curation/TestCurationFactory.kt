package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.ComplicationConfig
import com.hartwig.actin.clinical.curation.config.ECGConfig
import com.hartwig.actin.clinical.curation.config.InfectionConfig
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.MedicationCategoryConfig
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig
import com.hartwig.actin.clinical.curation.config.MedicationNameConfig
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig
import com.hartwig.actin.clinical.curation.config.ToxicityConfig
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory
import com.hartwig.actin.clinical.curation.translation.AdministrationRouteTranslation
import com.hartwig.actin.clinical.curation.translation.BloodTransfusionTranslation
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation
import com.hartwig.actin.clinical.curation.translation.ToxicityTranslation
import com.hartwig.actin.clinical.datamodel.ImmutableComplication
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.treatment.ImmutablePriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireRawEntryMapper
import com.hartwig.actin.doid.TestDoidModelFactory
import org.apache.logging.log4j.util.Strings
import java.util.*

object TestCurationFactory {
    fun createProperTestCurationModel(): CurationModel {
        return CurationModel(createTestCurationDatabase(), questionnaireRawEntryMapper())
    }

    fun createMinimalTestCurationModel(): CurationModel {
        return CurationModel(
            CurationDatabase(
                emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),
                emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),
                emptyList(), emptyList()
            ), questionnaireRawEntryMapper()
        )
    }

    fun createMinimalTestCurationDatabaseValidator(): CurationValidator {
        return CurationValidator(TestDoidModelFactory.createMinimalTestDoidModel())
    }

    fun createTestCurationDatabase(): CurationDatabase {
        return CurationDatabase(
            primaryTumorConfigs = createTestPrimaryTumorConfigs(),
            oncologicalHistoryConfigs = createTestOncologicalHistoryConfigs(),
            secondPrimaryConfigs = createTestSecondPrimaryConfigs(),
            lesionLocationConfigs = createTestLesionLocationConfigs(),
            nonOncologicalHistoryConfigs = createTestNonOncologicalHistoryConfigs(),
            ecgConfigs = createTestECGConfigs(),
            infectionConfigs = createTestInfectionConfigs(),
            complicationConfigs = createTestComplicationConfigs(),
            toxicityConfigs = createTestToxicityConfigs(),
            molecularTestConfigs = createTestMolecularTestConfigs(),
            medicationNameConfigs = createTestMedicationNameConfigs(),
            medicationDosageConfigs = createTestMedicationDosageConfigs(),
            medicationCategoryConfigs = createTestMedicationCategoryConfigs(),
            intoleranceConfigs = createTestIntoleranceConfigs(),
            administrationRouteTranslations = createTestAdministrationRouteTranslations(),
            laboratoryTranslations = createTestLaboratoryTranslations(),
            toxicityTranslations = createTestToxicityTranslations(),
            bloodTransfusionTranslations = createTestBloodTransfusionTranslations()
        )
    }

    private fun createTestPrimaryTumorConfigs(): List<PrimaryTumorConfig> {
        return listOf(
            PrimaryTumorConfig(
                input = "Unknown | Carcinoma",
                primaryTumorLocation = "Unknown",
                primaryTumorSubLocation = "CUP",
                primaryTumorType = "Carcinoma",
                primaryTumorSubType = Strings.EMPTY,
                primaryTumorExtraDetails = Strings.EMPTY,
                doids = setOf("299")
            )
        )
    }

    private fun createTestOncologicalHistoryConfigs(): List<OncologicalHistoryConfig> {
        return listOf(
            OncologicalHistoryConfig(
                input = "Cis 2020 2021",
                ignore = false,
                curated = ImmutablePriorTumorTreatment.builder()
                    .name("Cisplatin")
                    .startYear(2020)
                    .addCategories(TreatmentCategory.CHEMOTHERAPY)
                    .isSystemic(true)
                    .chemoType("platinum")
                    .build()
            ),
            OncologicalHistoryConfig(
                input = "Cis 2020 2021",
                ignore = false,
                curated = ImmutablePriorTumorTreatment.builder()
                    .name("Cisplatin")
                    .startYear(2021)
                    .addCategories(TreatmentCategory.CHEMOTHERAPY)
                    .isSystemic(true)
                    .chemoType("platinum")
                    .build()
            ),
            OncologicalHistoryConfig(input = "no systemic treatment", ignore = true, curated = null)
        )
    }

    private fun createTestSecondPrimaryConfigs(): List<SecondPrimaryConfig> {
        return listOf(
            SecondPrimaryConfig(
                input = "Breast cancer Jan-2018",
                ignore = false,
                curated = ImmutablePriorSecondPrimary.builder()
                    .tumorLocation("Breast")
                    .tumorSubLocation(Strings.EMPTY)
                    .tumorType("Carcinoma")
                    .tumorSubType(Strings.EMPTY)
                    .diagnosedYear(2018)
                    .diagnosedMonth(1)
                    .treatmentHistory("Surgery")
                    .isActive(false)
                    .build()
            )
        )
    }

    private fun createTestLesionLocationConfigs(): List<LesionLocationConfig> {
        return listOf(
            LesionLocationConfig(input = "Abdominal", location = "Abdominal"),
            LesionLocationConfig(input = "Lever", location = "Liver", category = LesionLocationCategory.LIVER),
            LesionLocationConfig(input = "Cns", location = "CNS", category = LesionLocationCategory.CNS),
            LesionLocationConfig(input = "Brain", location = "Brain", category = LesionLocationCategory.BRAIN),
            LesionLocationConfig(input = "Bone", location = "Bone", category = LesionLocationCategory.BONE),
            LesionLocationConfig(input = "Pulmonal", location = "Lung", category = LesionLocationCategory.LUNG),
            LesionLocationConfig(input = "Lymph node", location = "Lymph node", category = LesionLocationCategory.LYMPH_NODE),
            LesionLocationConfig(input = "Not a lesion", location = Strings.EMPTY),
            LesionLocationConfig(input = "No", location = Strings.EMPTY)
        )
    }

    private fun createTestNonOncologicalHistoryConfigs(): List<NonOncologicalHistoryConfig> {
        return listOf(
            NonOncologicalHistoryConfig(
                input = "sickness",
                ignore = false,
                lvef = Optional.empty(),
                priorOtherCondition = Optional.of(
                    ImmutablePriorOtherCondition.builder()
                        .name("sick")
                        .category("being sick")
                        .isContraindicationForTherapy(true)
                        .build()
                )
            ),
            NonOncologicalHistoryConfig(
                input = "not a condition",
                ignore = true,
                lvef = Optional.empty(),
                priorOtherCondition = Optional.empty()
            ),
            NonOncologicalHistoryConfig(
                input = "LVEF 0.17",
                ignore = false,
                lvef = Optional.of(0.17),
                priorOtherCondition = Optional.empty()
            )
        )
    }

    private fun createTestECGConfigs(): List<ECGConfig> {
        return listOf(
            ECGConfig(
                input = "Weird aberration",
                ignore = false,
                interpretation = "Cleaned aberration",
                isQTCF = false,
                isJTC = false
            ),
            ECGConfig(
                input = "No aberration",
                ignore = true,
                interpretation = Strings.EMPTY,
                isQTCF = false,
                isJTC = false
            ),
            ECGConfig(
                input = "Yes but unknown what aberration",
                ignore = false,
                interpretation = Strings.EMPTY,
                isQTCF = false,
                isJTC = false
            )
        )
    }

    private fun createTestInfectionConfigs(): List<InfectionConfig> {
        return listOf(
            InfectionConfig(input = "Weird infection", interpretation = "Cleaned infection"),
            InfectionConfig(input = "No infection", interpretation = Strings.EMPTY)
        )
    }

    private fun createTestComplicationConfigs(): List<ComplicationConfig> {
        return listOf(
            ComplicationConfig(
                input = "Term",
                ignore = false,
                impliesUnknownComplicationState = false,
                curated = ImmutableComplication.builder().name("Curated").addCategories("Curated category").build()
            ),
            ComplicationConfig(
                input = "Unknown",
                ignore = false,
                impliesUnknownComplicationState = true,
                curated = ImmutableComplication.builder().name(Strings.EMPTY).build()
            ),
            ComplicationConfig(
                input = "None",
                ignore = true,
                impliesUnknownComplicationState = false,
                curated = null
            ),
            ComplicationConfig(
                input = "vomit",
                ignore = false,
                impliesUnknownComplicationState = false,
                curated = ImmutableComplication.builder().name("Vomit").addCategories("Vomit category").build()
            )
        )
    }

    private fun createTestToxicityConfigs(): List<ToxicityConfig> {
        return listOf(
            ToxicityConfig(
                ignore = false,
                input = "neuropathy gr3",
                name = "neuropathy",
                categories = setOf("neuro"),
                grade = 3
            )
        )
    }

    private fun createTestMolecularTestConfigs(): List<MolecularTestConfig> {
        return listOf(
            MolecularTestConfig(
                input = "IHC ERBB2 3+",
                ignore = false,
                curated = ImmutablePriorMolecularTest.builder()
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
        )
    }

    private fun createTestMedicationNameConfigs(): List<MedicationNameConfig> {
        return listOf(
            MedicationNameConfig(input = "A en B", ignore = false, name = "A and B"),
            MedicationNameConfig(input = "No medication", ignore = true, name = Strings.EMPTY)
        )
    }

    private fun createTestMedicationDosageConfigs(): List<MedicationDosageConfig> {
        return listOf(
            MedicationDosageConfig(
                input = "50-60 mg per day",
                dosageMin = 50.0,
                dosageMax = 60.0,
                dosageUnit = "mg",
                frequency = 1.0,
                frequencyUnit = "day",
                ifNeeded = false
            )
        )
    }

    private fun createTestMedicationCategoryConfigs(): List<MedicationCategoryConfig> {
        return listOf(MedicationCategoryConfig(input = "Paracetamol", categories = setOf("Acetanilide derivatives")))
    }

    private fun createTestIntoleranceConfigs(): List<IntoleranceConfig> {
        return listOf(IntoleranceConfig(input = "Latex type 1", name = "Latex (type 1)", doids = setOf("0060532")))
    }

    private fun createTestAdministrationRouteTranslations(): List<AdministrationRouteTranslation> {
        return listOf(
            AdministrationRouteTranslation(
                administrationRoute = "ignore",
                translatedAdministrationRoute = Strings.EMPTY
            ),
            AdministrationRouteTranslation(
                administrationRoute = "oraal",
                translatedAdministrationRoute = "oral"
            )
        )
    }

    private fun createTestLaboratoryTranslations(): List<LaboratoryTranslation> {
        return listOf(LaboratoryTranslation(code = "CO", translatedCode = "CODE", name = "naam", translatedName = "Name"))
    }

    private fun createTestToxicityTranslations(): List<ToxicityTranslation> {
        return listOf(ToxicityTranslation(toxicity = "Pijn", translatedToxicity = "Pain"))
    }

    private fun createTestBloodTransfusionTranslations(): List<BloodTransfusionTranslation> {
        return listOf(
            BloodTransfusionTranslation(product = "Product", translatedProduct = "Translated product"),
            BloodTransfusionTranslation(product = "Not used", translatedProduct = "never used")
        )
    }

    private fun questionnaireRawEntryMapper(): QuestionnaireRawEntryMapper {
        return QuestionnaireRawEntryMapper(emptyMap())
    }
}