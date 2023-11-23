package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.clinical.curation.TestCurationFactory
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

class MedicationExtractorTest {
    private val extractor =
        MedicationExtractor(TestCurationFactory.createProperTestCurationDatabase(), TestAtcFactory.createProperAtcModel())

    @Test
    fun `Should interpret period between unit`() {
        assertThat(extractor.curatePeriodBetweenUnit(PATIENT_ID, null)).isNull()
        assertThat(extractor.curatePeriodBetweenUnit(PATIENT_ID, "")).isNull()
        assertThat(extractor.curatePeriodBetweenUnit(PATIENT_ID, "mo")).isEqualTo("months")
    }

    @Test
    fun `Should curate known medication dosage`() {
        val (medications, evaluation) = extractor.extract(PATIENT_ID, listOf(medicationEntryWithDosage(KNOWN_DOSAGE_INSTRUCTION)))
        assertThat(evaluation.warnings).isEmpty()
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
        val (medications, evaluation) = extractor.extract(PATIENT_ID, listOf(medicationEntryWithDosage(CANNOT_CURATE)))
        assertThat(medications).isEmpty()
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
        assertMedicationForName("A en B", listOf("A and B"))
    }

    private fun assertMedicationForName(
        inputText: String, expectedNames: List<String> = emptyList(), expectedWarnings: List<CurationWarning> = emptyList()
    ) {
        val (medications, evaluation) = extractor.extract(PATIENT_ID, listOf(medicationEntryWithName(inputText)))
        assertThat(medications.map(Medication::name)).isEqualTo(expectedNames)
        assertThat(evaluation.warnings).containsExactlyElementsOf(expectedWarnings)
    }

    @Test
    fun `Should curate medication status`() {
        assertThat(extractor.curateMedicationStatus(PATIENT_ID, "")).isNull()
        assertThat(extractor.curateMedicationStatus(PATIENT_ID, "active")).isEqualTo(MedicationStatus.ACTIVE)
        assertThat(extractor.curateMedicationStatus(PATIENT_ID, "on-hold")).isEqualTo(MedicationStatus.ON_HOLD)
        assertThat(extractor.curateMedicationStatus(PATIENT_ID, "Kuur geannuleerd")).isEqualTo(MedicationStatus.CANCELLED)
        assertThat(extractor.curateMedicationStatus(PATIENT_ID, "not a status")).isEqualTo(MedicationStatus.UNKNOWN)
    }

    @Test
    fun `Should translate administration route`() {
        assertThat(extractor.translateAdministrationRoute(PATIENT_ID, null)).isNull()
        assertThat(extractor.translateAdministrationRoute(PATIENT_ID, "")).isNull()
        assertThat(extractor.translateAdministrationRoute(PATIENT_ID, "not a route")).isNull()
        assertThat(extractor.translateAdministrationRoute(PATIENT_ID, "ignore")).isNull()
        assertThat(extractor.translateAdministrationRoute(PATIENT_ID, "oraal")).isEqualTo("oral")
    }

    private fun medicationEntryWithDosage(dosageInstruction: String) = TestFeedFactory.medicationEntry(
        "active", dosageInstruction, LocalDate.now(), LocalDate.now(), true, administrationRoute = "oral"
    )

    private fun medicationEntryWithName(name: String) = TestFeedFactory.medicationEntry(
        "active", KNOWN_DOSAGE_INSTRUCTION, LocalDate.now(), LocalDate.now(), true, codeText = name
    )
}