package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.translation.Translation
import com.hartwig.actin.clinical.datamodel.ImmutableDosage
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.MedicationStatus
import com.hartwig.actin.clinical.feed.TestFeedFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "cannot curate"
private const val KNOWN_DOSAGE_INSTRUCTION = "once per day 50-60 mg every month"
private const val KNOWN_MEDICATION_NAME = "A en B"

class MedicationExtractorTest {
    private val extractor =
        MedicationExtractor(TestCurationFactory.createProperTestCurationDatabase(), TestAtcFactory.createProperAtcModel())

    @Test
    fun `Should interpret period between unit`() {
        assertThat(extractor.curatePeriodBetweenUnit(PATIENT_ID, null).extracted).isNull()
        assertThat(extractor.curatePeriodBetweenUnit(PATIENT_ID, "").extracted).isNull()
        assertThat(extractor.curatePeriodBetweenUnit(PATIENT_ID, "mo").extracted).isEqualTo("months")
    }

    @Test
    fun `Should curate known medication dosage`() {
        val (medications, evaluation) = extractor.extract(PATIENT_ID, listOf(medicationEntryWithDosage(KNOWN_DOSAGE_INSTRUCTION)))
        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.medicationDosageEvaluatedInputs).containsExactly(KNOWN_DOSAGE_INSTRUCTION.lowercase())
        assertThat(medications).hasSize(1)
        assertThat(medications.first().dosage()).isEqualTo(
            ImmutableDosage.builder()
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
    fun `Should curate medication name`() {
        assertMedicationForName("", emptyList())
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
        assertMedicationForName("No medication", emptyList())
        assertMedicationForName(KNOWN_MEDICATION_NAME, listOf("A and B"))
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
        val (extracted, evaluation) = extractor.translateAdministrationRoute(PATIENT_ID, "oraal")
        assertThat(extracted).isEqualTo("oral")
        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.administrationRouteEvaluatedInputs).containsExactly(Translation("oraal", "oral"))
    }

    private fun medicationEntryWithDosage(dosageInstruction: String) = TestFeedFactory.medicationEntry(
        "active", dosageInstruction, LocalDate.now(), LocalDate.now(), true, administrationRoute = "oraal", codeText = KNOWN_MEDICATION_NAME
    )

    private fun medicationEntryWithName(name: String) = TestFeedFactory.medicationEntry(
        "active", "", LocalDate.now(), LocalDate.now(), true, administrationRoute = "ignore", codeText = name
    ).copy(dosageInstructionDoseQuantityUnit = "milligram")

    @Test
    fun `Should translate dosage unit`() {
        assertThat(extractor.translateDosageUnit(PATIENT_ID, null).extracted).isNull()
        val (translation, evaluation) = extractor.translateDosageUnit(PATIENT_ID, "STUK")
        assertThat(translation).isEqualTo("piece")
        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.dosageUnitEvaluatedInputs).containsExactly(Translation("stuk", "piece"))
    }

    @Test
    fun `Should not warn for empty dosage unit`() {
        val (emptyTranslation, evaluation) = extractor.translateDosageUnit(PATIENT_ID, "")
        assertThat(emptyTranslation).isNull()
        assertThat(evaluation.warnings).isEmpty()
    }
}