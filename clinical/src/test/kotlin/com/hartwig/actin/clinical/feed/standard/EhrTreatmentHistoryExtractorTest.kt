package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.datamodel.BodyLocationCategory
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentStage
import com.hartwig.actin.clinical.feed.standard.EhrTestData.createEhrPatientRecord
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

private const val PREVIOUS_CONDITION = "previous_condition"

private val PRIOR_CONDITION = EhrPriorOtherCondition(
    name = PREVIOUS_CONDITION,
    startDate = LocalDate.of(2024, 2, 27),
    endDate = LocalDate.of(2024, 2, 28),
)

private val TREATMENT = DrugTreatment("drug", drugs = emptySet())
private val MODIFICATION_TREATMENT = DrugTreatment("modification", drugs = emptySet())

private val TREATMENT_HISTORY = EhrTestData.createEhrTreatmentHistory()

private val TREATMENT_HISTORY_ENTRY_CONFIG = TreatmentHistoryEntryConfig(
    TREATMENT_NAME,
    false,
    TreatmentHistoryEntry(
        treatments = setOf(TREATMENT),
        treatmentHistoryDetails = TreatmentHistoryDetails(
            bodyLocations = setOf("bone"),
            bodyLocationCategories = setOf(BodyLocationCategory.BONE),
            maintenanceTreatment = TreatmentStage(treatment = TREATMENT, cycles = 1, startYear = 2024, startMonth = 2),
        ),
        trialAcronym = "trialAcronym",
    )
)

private val EHR_PATIENT_RECORD = createEhrPatientRecord()

private val TREATMENT_HISTORY_ENTRY = TreatmentHistoryEntry(
    startYear = 2024,
    startMonth = 2,
    treatments = TREATMENT_HISTORY_ENTRY_CONFIG.curated!!.treatments,
    treatmentHistoryDetails = TreatmentHistoryDetails(
        stopYear = 2024,
        stopMonth = 2,
        stopReason = StopReason.TOXICITY,
        bestResponse = TreatmentResponse.COMPLETE_RESPONSE,
        bodyLocations = setOf("bone"),
        bodyLocationCategories = setOf(BodyLocationCategory.BONE),
        switchToTreatments = listOf(TreatmentStage(treatment = MODIFICATION_TREATMENT, cycles = 2, startYear = 2024, startMonth = 2)),
        cycles = 1,
        maintenanceTreatment = TreatmentStage(treatment = TREATMENT, cycles = 1, startYear = 2024, startMonth = 2),
    ),
    isTrial = false,
    trialAcronym = "trialAcronym",
    intents = setOf(Intent.PALLIATIVE)
)

private val TREATMENT_HISTORY_FALLBACK = TREATMENT_HISTORY_ENTRY.copy(
    treatmentHistoryDetails = TREATMENT_HISTORY_ENTRY.treatmentHistoryDetails?.copy(
        switchToTreatments = listOf(
            TreatmentStage(
                treatment = TREATMENT,
                cycles = 2,
                startYear = 2024,
                startMonth = 2
            )
        )
    )
)

class EhrTreatmentHistoryExtractorTest {

    private val treatmentCurationDatabase = mockk<CurationDatabase<TreatmentHistoryEntryConfig>>()
    private val nonOncologicalHistoryCuration = mockk<CurationDatabase<NonOncologicalHistoryConfig>> {
        every { find(any()) } returns emptySet()
    }
    private val extractor = EhrTreatmentHistoryExtractor(treatmentCurationDatabase, nonOncologicalHistoryCuration)

    @Test
    fun `Should filter treatment history entry and warn when no curation for treatment name`() {
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns emptySet()
        val result = extractor.extract(
            EHR_PATIENT_RECORD.copy(
                treatmentHistory = listOf(
                    TREATMENT_HISTORY
                )
            )
        )
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                message = "Could not find treatment history config for input 'treatmentName'",
                patientId = HASHED_ID_IN_BASE64,
                category = CurationCategory.ONCOLOGICAL_HISTORY,
                feedInput = TREATMENT_NAME
            )
        )
    }

    @Test
    fun `Should extract treatment with modifications using curated treatment`() {
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns setOf(TREATMENT_HISTORY_ENTRY_CONFIG)
        every { treatmentCurationDatabase.find(MODIFICATION_NAME) } returns setOf(
            TREATMENT_HISTORY_ENTRY_CONFIG.copy(
                curated = TREATMENT_HISTORY_ENTRY.copy(
                    treatments = setOf(MODIFICATION_TREATMENT)
                )
            )
        )
        val result = extractor.extract(
            EHR_PATIENT_RECORD.copy(
                treatmentHistory = listOf(TREATMENT_HISTORY)
            )
        )
        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted).isEqualTo(
            listOf(
                TREATMENT_HISTORY_ENTRY
            )
        )
    }

    @Test
    fun `Should fallback to original treatment for modification when no curation available and return a warning`() {
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns setOf(
            TREATMENT_HISTORY_ENTRY_CONFIG
        )
        every { treatmentCurationDatabase.find(MODIFICATION_NAME) } returns emptySet()
        val result = extractor.extract(
            EHR_PATIENT_RECORD.copy(
                treatmentHistory = listOf(TREATMENT_HISTORY)
            )
        )
        assertThat(result.extracted).containsExactly(TREATMENT_HISTORY_FALLBACK)
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                message = "Could not find treatment history config for input '$MODIFICATION_NAME'",
                patientId = HASHED_ID_IN_BASE64,
                category = CurationCategory.ONCOLOGICAL_HISTORY,
                feedInput = MODIFICATION_NAME
            )
        )
    }

    @Test
    fun `Should fallback to original treatment for modification and return no warnings if curation is ignored`() {
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns setOf(
            TREATMENT_HISTORY_ENTRY_CONFIG
        )
        every { treatmentCurationDatabase.find(MODIFICATION_NAME) } returns setOf(
            TREATMENT_HISTORY_ENTRY_CONFIG.copy(ignore = true)
        )
        val result = extractor.extract(
            EHR_PATIENT_RECORD.copy(
                treatmentHistory = listOf(TREATMENT_HISTORY)
            )
        )
        assertThat(result.extracted).containsExactly(TREATMENT_HISTORY_FALLBACK)
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should throw exception if modification cannot fall back to original treatment`() {
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns setOf(
            TREATMENT_HISTORY_ENTRY_CONFIG.copy(curated = TREATMENT_HISTORY_ENTRY.copy(treatments = emptySet()))
        )
        every { treatmentCurationDatabase.find(MODIFICATION_NAME) } returns emptySet()
        assertThatThrownBy {
            extractor.extract(
                EHR_PATIENT_RECORD.copy(
                    treatmentHistory = listOf(TREATMENT_HISTORY)
                )
            )
        }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `Should consider previous conditions as treatment history if not present in non-oncological history curation and is present in oncological history curation`() {
        every { treatmentCurationDatabase.find(PREVIOUS_CONDITION) } returns setOf(TREATMENT_HISTORY_ENTRY_CONFIG)
        val result = extractor.extract(
            EHR_PATIENT_RECORD.copy(
                treatmentHistory = emptyList(),
                priorOtherConditions = listOf(
                    PRIOR_CONDITION
                )
            )
        )
        assertThat(result.extracted).containsExactly(
            TreatmentHistoryEntry(
                startYear = 2024,
                startMonth = 2,
                treatments = TREATMENT_HISTORY_ENTRY_CONFIG.curated!!.treatments,
                treatmentHistoryDetails = TreatmentHistoryDetails(
                    stopYear = 2024,
                    stopMonth = 2,
                    stopReason = null,
                    bestResponse = null,
                    bodyLocations = setOf("bone"),
                    bodyLocationCategories = setOf(BodyLocationCategory.BONE),
                    switchToTreatments = null,
                    cycles = null,
                    maintenanceTreatment = TreatmentStage(treatment = TREATMENT, cycles = 1, startYear = 2024, startMonth = 2),
                ),
                isTrial = false,
                trialAcronym = "trialAcronym"
            )
        )
    }

    @Test
    fun `Should skip previous conditions present in the non-oncological curation`() {
        every { nonOncologicalHistoryCuration.find(PREVIOUS_CONDITION) } returns setOf(
            NonOncologicalHistoryConfig(
                input = PREVIOUS_CONDITION,
                ignore = false
            )
        )
        val result = extractor.extract(
            EHR_PATIENT_RECORD.copy(
                treatmentHistory = emptyList(),
                priorOtherConditions = listOf(PRIOR_CONDITION)
            )
        )
        assertThat(result.extracted).isEmpty()
    }

    @Test
    fun `Should include evaluated input when prior condition is ignored in both prior condition and oncological history curation`() {
        every { nonOncologicalHistoryCuration.find(PREVIOUS_CONDITION) } returns setOf(
            NonOncologicalHistoryConfig(
                input = PREVIOUS_CONDITION,
                ignore = true
            )
        )
        every { treatmentCurationDatabase.find(PREVIOUS_CONDITION) } returns setOf(
            TREATMENT_HISTORY_ENTRY_CONFIG.copy(
                ignore = true,
                input = PREVIOUS_CONDITION
            )
        )
        val result = extractor.extract(
            EHR_PATIENT_RECORD.copy(
                treatmentHistory = listOf(TREATMENT_HISTORY.copy(treatmentName = PREVIOUS_CONDITION, modifications = null)),
            )
        )
        assertThat(result.evaluation.treatmentHistoryEntryEvaluatedInputs).containsExactly(PREVIOUS_CONDITION)
    }

    @Test
    fun `Should ignore entries when configured and curated is null`() {
        every { nonOncologicalHistoryCuration.find(TREATMENT_NAME) } returns emptySet()
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns setOf(
            TREATMENT_HISTORY_ENTRY_CONFIG.copy(
                ignore = true,
                input = TREATMENT_NAME,
                curated = null
            )
        )
        val result = extractor.extract(
            EHR_PATIENT_RECORD.copy(
                treatmentHistory = listOf(TREATMENT_HISTORY),
            )
        )
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.treatmentHistoryEntryEvaluatedInputs).containsExactly(TREATMENT_NAME.lowercase())
    }
}