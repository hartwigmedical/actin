package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.clinical.curation.ANATOMICAL
import com.hartwig.actin.clinical.curation.CHEMICAL
import com.hartwig.actin.clinical.curation.CHEMICAL_SUBSTANCE
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.FULL_ATC_CODE
import com.hartwig.actin.clinical.curation.PHARMACOLOGICAL
import com.hartwig.actin.clinical.curation.THERAPEUTIC
import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.DrugInteractionConfig
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig
import com.hartwig.actin.clinical.curation.config.MedicationNameConfig
import com.hartwig.actin.clinical.curation.config.PeriodBetweenUnitConfig
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfig
import com.hartwig.actin.clinical.curation.translation.Translation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.feed.emc.TestFeedFactory
import com.hartwig.actin.datamodel.clinical.AtcClassification
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.DrugInteraction
import com.hartwig.actin.datamodel.clinical.Dosage
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.MedicationStatus
import com.hartwig.actin.datamodel.clinical.QTProlongatingRisk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "cannot curate"

private const val ADMINISTRATION_ROUTE_INPUT_ORAL = "Oraal"

private const val DOSAGE_INPUT = "Dosage input"
private const val DOSAGE_UNIT_TRANSLATED_MG = "mg"

private val DOSAGE = Dosage(
    dosageMin = 50.0,
    dosageMax = 60.0,
    dosageUnit = DOSAGE_UNIT_TRANSLATED_MG,
    frequency = 1.0,
    frequencyUnit = "day",
    periodBetweenValue = 1.0,
    periodBetweenUnit = "mo",
    ifNeeded = false
)

private val cypInteraction = DrugInteraction(name = "2D6", strength = DrugInteraction.Strength.WEAK, type = DrugInteraction.Type.INHIBITOR)
private val bcrpInteraction =
    DrugInteraction(name = "BCRP", strength = DrugInteraction.Strength.UNKNOWN, type = DrugInteraction.Type.SUBSTRATE)

private const val TRANSLATED_ADMINISTRATION_ROUTE_ORAL = "oral"

private const val CURATED_MEDICATION_NAME = "Paracetamol"

private const val MEDICATION_NAME_INPUT = "PARACETAMOL"

private const val PERIOD_BETWEEN_UNIT_INPUT = "Period between unit input"

private const val CURATED_PERIOD_BETWEEN_UNIT = "Curated period between unit"

private const val NO_MEDICATION_NAME_INPUT = "No medication name input"

private const val DOSAGE_TRANSLATION_INPUT_MILLIGRAM = "milligram"

class MedicationExtractorTest {

    private val extractor =
        MedicationExtractor(
            TestCurationFactory.curationDatabase(
                MedicationNameConfig(
                    input = MEDICATION_NAME_INPUT,
                    ignore = false,
                    name = CURATED_MEDICATION_NAME
                ),
                MedicationNameConfig(
                    input = NO_MEDICATION_NAME_INPUT,
                    ignore = true,
                    name = ""
                )
            ),
            TestCurationFactory.curationDatabase(
                MedicationDosageConfig(
                    input = DOSAGE_INPUT,
                    ignore = false,
                    curated = DOSAGE
                )
            ),
            TestCurationFactory.curationDatabase(
                PeriodBetweenUnitConfig(
                    input = PERIOD_BETWEEN_UNIT_INPUT,
                    ignore = false,
                    interpretation = CURATED_PERIOD_BETWEEN_UNIT
                )
            ),
            TestCurationFactory.curationDatabase(
                DrugInteractionConfig(
                    input = CURATED_MEDICATION_NAME,
                    ignore = false,
                    cypInteractions = listOf(cypInteraction),
                    transporterInteractions = listOf(bcrpInteraction)
                )
            ),
            TestCurationFactory.curationDatabase(
                QTProlongatingConfig(
                    input = CURATED_MEDICATION_NAME,
                    ignore = false,
                    status = QTProlongatingRisk.POSSIBLE
                )
            ),
            TranslationDatabase(
                mapOf(
                    ADMINISTRATION_ROUTE_INPUT_ORAL to Translation(
                        ADMINISTRATION_ROUTE_INPUT_ORAL,
                        TRANSLATED_ADMINISTRATION_ROUTE_ORAL
                    )
                ), CurationCategory.ADMINISTRATION_ROUTE_TRANSLATION
            ) { emptySet() },
            TranslationDatabase(
                mapOf(
                    DOSAGE_TRANSLATION_INPUT_MILLIGRAM to Translation(
                        DOSAGE_TRANSLATION_INPUT_MILLIGRAM,
                        DOSAGE_UNIT_TRANSLATED_MG
                    )
                ), CurationCategory.DOSAGE_UNIT_TRANSLATION
            ) { emptySet() },
            TestAtcFactory.createProperAtcModel(),
            TestTreatmentDatabaseFactory.createProper()
        )


    @Test
    fun `Should extract all medication fields`() {
        val entry = TestFeedFactory.medicationEntry(
            status = "active",
            dosageInstruction = DOSAGE_INPUT,
            start = LocalDate.of(2023, 12, 12),
            end = LocalDate.of(2023, 12, 13),
            active = true,
            code5ATCCode = FULL_ATC_CODE,
            code5ATCDisplay = MEDICATION_NAME_INPUT,
            administrationRoute = ADMINISTRATION_ROUTE_INPUT_ORAL
        )

        assertThat(extractor.extract(PATIENT_ID, listOf(entry)).extracted).containsExactly(
            Medication(
                name = CURATED_MEDICATION_NAME,
                status = MedicationStatus.ACTIVE,
                administrationRoute = "oral",
                dosage = DOSAGE,
                startDate = LocalDate.of(2023, 12, 12),
                stopDate = LocalDate.of(2023, 12, 13),
                cypInteractions = listOf(cypInteraction),
                transporterInteractions = listOf(bcrpInteraction),
                qtProlongatingRisk = QTProlongatingRisk.POSSIBLE,
                atc = AtcClassification(
                    anatomicalMainGroup = AtcLevel("N", ANATOMICAL),
                    therapeuticSubGroup = AtcLevel("N02", THERAPEUTIC),
                    pharmacologicalSubGroup = AtcLevel("N02B", PHARMACOLOGICAL),
                    chemicalSubGroup = AtcLevel("N02BE", CHEMICAL),
                    chemicalSubstance = AtcLevel(FULL_ATC_CODE, CHEMICAL_SUBSTANCE)
                ),
                isSelfCare = false,
                isTrialMedication = false
            )
        )
    }

    @Test
    fun `Should not return medications for entries with no ATC display or code text`() {
        val entry = TestFeedFactory.medicationEntry(
            status = "",
            dosageInstruction = "Irrelevant",
            start = LocalDate.of(2019, 2, 2),
            end = LocalDate.of(2019, 4, 4),
            active = false,
        )
        assertThat(extractor.extract(PATIENT_ID, listOf(entry)).extracted).isEmpty()
    }

    @Test
    fun `Should interpret as self-care when ATC code and display are empty`() {
        val entry = TestFeedFactory.medicationEntry(
            status = "",
            dosageInstruction = "",
            start = LocalDate.of(2019, 2, 2),
            end = LocalDate.of(2019, 4, 4),
            active = false,
            codeText = MEDICATION_NAME_INPUT
        )

        assertThat(extractor.extract(PATIENT_ID, listOf(entry)).extracted.first().isSelfCare).isTrue
    }

    @Test
    fun `Should interpret as trial medication when ATC display is empty and ATC code does not start with letter`() {
        val entry = TestFeedFactory.medicationEntry(
            status = "",
            dosageInstruction = "",
            start = LocalDate.of(2019, 2, 2),
            end = LocalDate.of(2019, 4, 4),
            active = false,
            code5ATCCode = "123",
            codeText = MEDICATION_NAME_INPUT
        )

        assertThat(extractor.extract(PATIENT_ID, listOf(entry)).extracted.first().isTrialMedication).isTrue
    }

    @Test
    fun `Should interpret period between unit`() {
        assertThat(extractor.curatePeriodBetweenUnit(PATIENT_ID, null).extracted).isNull()
        assertThat(extractor.curatePeriodBetweenUnit(PATIENT_ID, "").extracted).isNull()
        assertThat(
            extractor.curatePeriodBetweenUnit(
                PATIENT_ID,
                PERIOD_BETWEEN_UNIT_INPUT
            ).extracted
        ).isEqualTo(CURATED_PERIOD_BETWEEN_UNIT)
    }

    @Test
    fun `Should curate known medication dosage`() {
        val (medications, evaluation) = extractor.extract(PATIENT_ID, listOf(medicationEntryWithDosage(" $DOSAGE_INPUT ")))
        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.medicationDosageEvaluatedInputs).containsExactly(DOSAGE_INPUT.lowercase())
        assertThat(medications).hasSize(1)
        assertThat(medications.first().dosage).isEqualTo(DOSAGE)
    }

    @Test
    fun `Should create warning for unknown medication dosage`() {
        val evaluation = extractor.extract(PATIENT_ID, listOf(medicationEntryWithDosage(CANNOT_CURATE))).evaluation
        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.MEDICATION_DOSAGE,
                CANNOT_CURATE,
                "Could not find medication dosage config for input '$CANNOT_CURATE'"
            )
        )
    }

    @Test
    fun `Should ignore empty medication names`() {
        assertMedicationForName("", emptyList())
    }

    @Test
    fun `Should create warning for input medication name that cannot be curated`() {
        assertMedicationForName(
            CANNOT_CURATE, emptyList(), listOf(
                CurationWarning(
                    PATIENT_ID,
                    CurationCategory.MEDICATION_NAME,
                    CANNOT_CURATE,
                    "Could not find medication name config for input '$CANNOT_CURATE'"
                )
            )
        )
        assertMedicationForName(NO_MEDICATION_NAME_INPUT, emptyList())
        assertMedicationForName(MEDICATION_NAME_INPUT, listOf(CURATED_MEDICATION_NAME))
    }

    @Test
    fun `Should not extract names for medication name inputs that are mapped to configs with ignore = true`() {
        assertMedicationForName(NO_MEDICATION_NAME_INPUT, emptyList())
    }

    @Test
    fun `Should extract correct medication name for known medication name`() {
        assertMedicationForName(MEDICATION_NAME_INPUT, listOf(CURATED_MEDICATION_NAME))
    }

    private fun assertMedicationForName(
        inputText: String, expectedNames: List<String> = emptyList(), expectedWarnings: List<CurationWarning> = emptyList()
    ) {
        val (medications, evaluation) = extractor.extract(PATIENT_ID, listOf(medicationEntryWithName(inputText)))
        assertThat(medications.map(Medication::name)).isEqualTo(expectedNames)
        assertThat(evaluation.warnings).containsExactlyElementsOf(expectedWarnings)
        assertThat(evaluation.medicationNameEvaluatedInputs).containsExactly(inputText.lowercase())
    }

    @Test
    fun `Should curate medication status`() {
        assertStatusExtraction("", null)
        assertStatusExtraction("active", MedicationStatus.ACTIVE)
        assertStatusExtraction("on-hold", MedicationStatus.ON_HOLD)
        assertStatusExtraction("Kuur geannuleerd", MedicationStatus.CANCELLED)
        assertStatusExtraction("not a status", MedicationStatus.UNKNOWN)
    }

    private fun assertStatusExtraction(input: String, expected: MedicationStatus?) {
        assertThat(extractor.curateMedicationStatus(PATIENT_ID, input)).isEqualTo(expected)
    }

    @Test
    fun `Should translate administration route`() {
        assertThat(extractor.translateAdministrationRoute(PATIENT_ID, null).extracted).isNull()
        assertThat(extractor.translateAdministrationRoute(PATIENT_ID, "").extracted).isNull()
        assertThat(extractor.translateAdministrationRoute(PATIENT_ID, "not a route").extracted).isNull()
        assertThat(extractor.translateAdministrationRoute(PATIENT_ID, "ignore").extracted).isNull()
        val (extracted, evaluation) = extractor.translateAdministrationRoute(PATIENT_ID, ADMINISTRATION_ROUTE_INPUT_ORAL)
        assertThat(extracted).isEqualTo(TRANSLATED_ADMINISTRATION_ROUTE_ORAL)
        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.administrationRouteEvaluatedInputs).containsExactly(
            Translation(
                ADMINISTRATION_ROUTE_INPUT_ORAL,
                TRANSLATED_ADMINISTRATION_ROUTE_ORAL
            )
        )
    }

    private fun medicationEntryWithDosage(dosageInstruction: String) = TestFeedFactory.medicationEntry(
        "active",
        dosageInstruction,
        LocalDate.now(),
        LocalDate.now(),
        true,
        administrationRoute = ADMINISTRATION_ROUTE_INPUT_ORAL,
        codeText = MEDICATION_NAME_INPUT
    )

    private fun medicationEntryWithName(name: String) = TestFeedFactory.medicationEntry(
        "active", "", LocalDate.now(), LocalDate.now(), true, administrationRoute = ADMINISTRATION_ROUTE_INPUT_ORAL, codeText = name
    ).copy(dosageInstructionDoseQuantityUnit = DOSAGE_TRANSLATION_INPUT_MILLIGRAM)

    @Test
    fun `Should translate dosage unit`() {
        assertThat(extractor.translateDosageUnit(PATIENT_ID, null).extracted).isNull()
        val (translation, evaluation) = extractor.translateDosageUnit(PATIENT_ID, DOSAGE_TRANSLATION_INPUT_MILLIGRAM)
        assertThat(translation).isEqualTo(DOSAGE_UNIT_TRANSLATED_MG)
        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.dosageUnitEvaluatedInputs).containsExactly(
            Translation(
                DOSAGE_TRANSLATION_INPUT_MILLIGRAM,
                DOSAGE_UNIT_TRANSLATED_MG
            )
        )
    }

    @Test
    fun `Should not warn for empty dosage unit`() {
        val (emptyTranslation, evaluation) = extractor.translateDosageUnit(PATIENT_ID, "")
        assertThat(emptyTranslation).isNull()
        assertThat(evaluation.warnings).isEmpty()
    }
}