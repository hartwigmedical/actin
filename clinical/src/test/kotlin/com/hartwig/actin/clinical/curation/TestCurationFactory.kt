package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.ComplicationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.CypInteractionConfig
import com.hartwig.actin.clinical.curation.config.ECGConfig
import com.hartwig.actin.clinical.curation.config.InfectionConfig
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig
import com.hartwig.actin.clinical.curation.config.MedicationNameConfig
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.config.PeriodBetweenUnitConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfig
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig
import com.hartwig.actin.clinical.curation.config.ToxicityConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory
import com.hartwig.actin.clinical.curation.translation.LaboratoryIdentifiers
import com.hartwig.actin.clinical.curation.translation.Translation
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.clinical.datamodel.ImmutableComplication
import com.hartwig.actin.clinical.datamodel.ImmutableCypInteraction
import com.hartwig.actin.clinical.datamodel.ImmutableDosage
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk
import com.hartwig.actin.clinical.datamodel.TumorStatus
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.feed.questionnaire.Questionnaire
import com.hartwig.actin.doid.TestDoidModelFactory
import org.apache.logging.log4j.util.Strings
import java.time.LocalDate

private const val PARACETAMOL = "PARACETAMOL"

object TestCurationFactory {

    fun createMinimalTestCurationDatabaseValidator(): CurationValidator {
        return CurationValidator(TestDoidModelFactory.createMinimalTestDoidModel())
    }

    fun createMinimalTestCurationDatabase(): CurationDatabase {
        return CurationDatabase(
            emptyList(), emptyMap(), emptyMap(), emptyMap()
        )
    }

    fun createProperTestCurationDatabase(): CurationDatabase {
        return CurationDatabase(
            validationErrors = emptyList(),
            configs = configsToMap(createTestPrimaryTumorConfigs()) +
                    configsToMap(createTestTreatmentHistoryEntryConfigs()) +
                    configsToMap(createTestSecondPrimaryConfigs()) +
                    configsToMap(createTestLesionLocationConfigs()) +
                    configsToMap(createTestNonOncologicalHistoryConfigs()) +
                    configsToMap(createTestECGConfigs()) +
                    configsToMap(createTestInfectionConfigs()) +
                    configsToMap(createTestPeriodBetweenUnitConfigs()) +
                    configsToMap(createTestComplicationConfigs()) +
                    configsToMap(createTestToxicityConfigs()) +
                    configsToMap(createTestMolecularTestConfigs()) +
                    configsToMap(createTestMedicationNameConfigs()) +
                    configsToMap(createTestMedicationDosageConfigs()) +
                    configsToMap(createTestIntoleranceConfigs()) +
                    configsToMap(createTestCypInteractionConfig()) +
                    configsToMap(createTestQTProlongingConfigs()),
            translations = createTestAdministrationRouteTranslations().associateBy { it.input } +
                    createTestDosageUnitTranslations().associateBy { it.input } +
                    createTestToxicityTranslations().associateBy { it.input } +
                    createTestBloodTransfusionTranslations().associateBy { it.input },
            laboratoryTranslations = createTestLaboratoryTranslations().associateBy { Pair(it.code, it.name) },
        )
    }

    fun emptyQuestionnaire(): Questionnaire {
        return Questionnaire(
            LocalDate.now(), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null
        )
    }

    private inline fun <reified T : CurationConfig> configsToMap(configs: List<T>): Map<Pair<String, Class<T>>, Set<T>> {
        return configs.groupBy { it.input.lowercase() }.mapKeys { it.key to T::class.java }.mapValues { it.value.toSet() }
    }

    private fun createTestQTProlongingConfigs(): List<QTProlongatingConfig> {
        return listOf(QTProlongatingConfig(PARACETAMOL, false, QTProlongatingRisk.POSSIBLE))
    }

    private fun createTestCypInteractionConfig(): List<CypInteractionConfig> {
        return listOf(CypInteractionConfig(PARACETAMOL, false, listOf(createTestCypInteraction())))
    }

    fun createTestCypInteraction(): ImmutableCypInteraction =
        ImmutableCypInteraction.builder().cyp("2D6").strength(CypInteraction.Strength.WEAK).type(CypInteraction.Type.INHIBITOR).build()

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
            ),
            PrimaryTumorConfig(
                input = "Stomach |",
                primaryTumorLocation = "Stomach",
                primaryTumorSubLocation = Strings.EMPTY,
                primaryTumorType = Strings.EMPTY,
                primaryTumorSubType = Strings.EMPTY,
                primaryTumorExtraDetails = Strings.EMPTY,
                doids = setOf("10534")
            ),
            PrimaryTumorConfig(
                input = "| Carcinoma",
                primaryTumorLocation = Strings.EMPTY,
                primaryTumorSubLocation = Strings.EMPTY,
                primaryTumorType = "Carcinoma",
                primaryTumorSubType = Strings.EMPTY,
                primaryTumorExtraDetails = Strings.EMPTY,
                doids = setOf("299")
            )
        )
    }

    private fun createTestTreatmentHistoryEntryConfigs(): List<TreatmentHistoryEntryConfig> {
        val cisplatin = ImmutableDrug.builder()
            .name("Cisplatin")
            .addDrugTypes(DrugType.PLATINUM_COMPOUND)
            .category(TreatmentCategory.CHEMOTHERAPY)
            .build()

        val therapy =
            ImmutableDrugTreatment.builder().name("Cisplatin").addDrugs(cisplatin).isSystemic(true).build()
        return listOf(
            TreatmentHistoryEntryConfig(
                input = "Cis 2020 2021",
                ignore = false,
                curated = ImmutableTreatmentHistoryEntry.builder().addTreatments(therapy).startYear(2020)
                    .build()
            ),
            TreatmentHistoryEntryConfig(
                input = "Cis 2020 2021",
                ignore = false,
                curated = ImmutableTreatmentHistoryEntry.builder().addTreatments(therapy).startYear(2021)
                    .build()
            ),
            TreatmentHistoryEntryConfig(input = "no systemic treatment", ignore = true, curated = null)
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
                    .status(TumorStatus.INACTIVE)
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
                lvef = null,
                priorOtherCondition = ImmutablePriorOtherCondition.builder()
                    .name("sick")
                    .category("being sick")
                    .isContraindicationForTherapy(true)
                    .build()

            ),
            NonOncologicalHistoryConfig(
                input = "not a condition",
                ignore = true,
                lvef = null,
                priorOtherCondition = null
            ),
            NonOncologicalHistoryConfig(
                input = "LVEF 0.17",
                ignore = false,
                lvef = null,
                priorOtherCondition = null
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

    private fun createTestPeriodBetweenUnitConfigs(): List<PeriodBetweenUnitConfig> {
        return listOf(
            PeriodBetweenUnitConfig(input = "mo", interpretation = "months"),
            PeriodBetweenUnitConfig(input = "", interpretation = Strings.EMPTY)
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
                input = "once per day 50-60 mg every month",
                curated = ImmutableDosage.builder()
                    .dosageMin(50.0)
                    .dosageMax(60.0)
                    .dosageUnit("mg")
                    .frequency(1.0)
                    .frequencyUnit("day")
                    .periodBetweenValue(1.0)
                    .periodBetweenUnit("mo")
                    .ifNeeded(false)
                    .build()
            )
        )
    }

    private fun createTestIntoleranceConfigs(): List<IntoleranceConfig> {
        return listOf(IntoleranceConfig(input = "Latex type 1", name = "Latex (type 1)", doids = setOf("0060532")))
    }

    private fun createTestAdministrationRouteTranslations(): List<Translation> {
        return listOf(
            Translation(
                input = "ignore",
                translated = Strings.EMPTY
            ),
            Translation(
                input = "oraal",
                translated = "oral"
            )
        )
    }

    private fun createTestLaboratoryTranslations(): List<LaboratoryIdentifiers> {
        return listOf(LaboratoryIdentifiers(code = "CO", translatedCode = "CODE", name = "naam", translatedName = "Name"))
    }

    private fun createTestToxicityTranslations(): List<Translation> {
        return listOf(Translation(input = "Pijn", translated = "Pain"))
    }

    private fun createTestBloodTransfusionTranslations(): List<Translation> {
        return listOf(
            Translation(input = "Product", translated = "Translated product"),
            Translation(input = "Not used", translated = "never used")
        )
    }

    private fun createTestDosageUnitTranslations(): List<Translation> {
        return listOf(
            Translation(input = "stuk", translated = "piece"),
            Translation(input = "milligram", translated = "mg")
        )
    }
}