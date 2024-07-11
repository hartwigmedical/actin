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

private val PROVIDED_TREATMENT_START = LocalDate.of(2021, 4, 1)
private val PROVIDED_TREATMENT_END = LocalDate.of(2021, 5, 1)
private val PROVIDED_MODIFICATION_START = LocalDate.of(2021, 6, 1)
private val PROVIDED_EHR_PATIENT_RECORD = createEhrPatientRecord()
private val PROVIDED_TREATMENT_HISTORY = EhrTestData.createEhrTreatmentHistory()
    .copy(
        treatmentName = TREATMENT_NAME,
        startDate = PROVIDED_TREATMENT_START,
        endDate = PROVIDED_TREATMENT_END,
        modifications = listOf(EhrTestData.createEhrModification().copy(name = MODIFICATION_NAME, date = PROVIDED_MODIFICATION_START))
    )

private const val PREVIOUS_CONDITION = "previous_condition"
private val PROVIDED_PRIOR_CONDITION_START = LocalDate.of(2022, 7, 1)
private val PROVIDED_PRIOR_CONDITION_END = LocalDate.of(2022, 8, 1)
private val PRIOR_OTHER_PRIOR_CONDITION = ProvidedPriorOtherCondition(
    name = PREVIOUS_CONDITION,
    startDate = PROVIDED_PRIOR_CONDITION_START,
    endDate = PROVIDED_PRIOR_CONDITION_END,
)
private val TREATMENT = DrugTreatment("drug", drugs = emptySet())
private val MODIFICATION_TREATMENT = DrugTreatment("modification", drugs = emptySet())

private val CURATED_TREATMENT_START = LocalDate.of(2020, 1, 1)
private val CURATED_TREATMENT_END = LocalDate.of(2020, 2, 1)
private val CURATED_MODIFICATION_START = LocalDate.of(2020, 3, 1)
private val CURATED_TREATMENT_STAGE = TreatmentStage(
    treatment = TREATMENT,
    cycles = 1,
    startYear = CURATED_TREATMENT_START.year,
    startMonth = CURATED_TREATMENT_END.monthValue
)
private val CURATED_TREATMENT_HISTORY_ENTRY = TreatmentHistoryEntryConfig(
    TREATMENT_NAME,
    false,
    TreatmentHistoryEntry(
        treatments = setOf(TREATMENT),
        treatmentHistoryDetails = TreatmentHistoryDetails(
            bodyLocations = setOf("bone"),
            bodyLocationCategories = setOf(BodyLocationCategory.BONE),
            maintenanceTreatment = CURATED_TREATMENT_STAGE
        ),
        trialAcronym = "trialAcronym",
    )
)
private val CURATED_MODIFICATION_TREATMENT_HISTORY_ENTRY = TreatmentHistoryEntryConfig(
    TREATMENT_NAME,
    false,
    TreatmentHistoryEntry(
        treatments = setOf(MODIFICATION_TREATMENT),
        treatmentHistoryDetails = TreatmentHistoryDetails()
    )
)

private val EXPECTED_TREATMENT_HISTORY_ENTRY = TreatmentHistoryEntry(
    startYear = PROVIDED_TREATMENT_START.year,
    startMonth = PROVIDED_TREATMENT_START.monthValue,
    treatments = CURATED_TREATMENT_HISTORY_ENTRY.curated!!.treatments,
    treatmentHistoryDetails = TreatmentHistoryDetails(
        stopYear = PROVIDED_TREATMENT_END.year,
        stopMonth = PROVIDED_TREATMENT_END.monthValue,
        stopReason = StopReason.TOXICITY,
        bestResponse = TreatmentResponse.COMPLETE_RESPONSE,
        bodyLocations = setOf("bone"),
        bodyLocationCategories = setOf(BodyLocationCategory.BONE),
        switchToTreatments = listOf(
            TreatmentStage(
                treatment = MODIFICATION_TREATMENT,
                cycles = 2,
                startYear = PROVIDED_MODIFICATION_START.year,
                startMonth = PROVIDED_MODIFICATION_START.monthValue
            )
        ),
        cycles = 1,
        maintenanceTreatment = CURATED_TREATMENT_STAGE,
    ),
    isTrial = false,
    trialAcronym = "trialAcronym",
    intents = setOf(Intent.PALLIATIVE)
)
private val EXPECTED_TREATMENT_HISTORY_FALLBACK = EXPECTED_TREATMENT_HISTORY_ENTRY.copy(
    treatmentHistoryDetails = EXPECTED_TREATMENT_HISTORY_ENTRY.treatmentHistoryDetails?.copy(
        switchToTreatments = listOf(
            TreatmentStage(
                treatment = TREATMENT,
                cycles = 2,
                startYear = PROVIDED_MODIFICATION_START.year,
                startMonth = PROVIDED_MODIFICATION_START.monthValue
            )
        )
    )
)

class StandardOncologicalHistoryExtractorTest {

    private val treatmentCurationDatabase = mockk<CurationDatabase<TreatmentHistoryEntryConfig>>()
    private val nonOncologicalHistoryCuration = mockk<CurationDatabase<NonOncologicalHistoryConfig>> {
        every { find(any()) } returns emptySet()
    }
    private val extractor = StandardOncologicalHistoryExtractor(treatmentCurationDatabase, nonOncologicalHistoryCuration)

    @Test
    fun `Should filter treatment history entry and warn when no curation for treatment name`() {
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns emptySet()
        val result = extractor.extract(
            PROVIDED_EHR_PATIENT_RECORD.copy(
                treatmentHistory = listOf(
                    PROVIDED_TREATMENT_HISTORY
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
    fun `Should extract treatment with modifications using curated treatment, curated modification and provided response, stop reason and stop date`() {
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns setOf(CURATED_TREATMENT_HISTORY_ENTRY)
        every { treatmentCurationDatabase.find(MODIFICATION_NAME) } returns setOf(CURATED_MODIFICATION_TREATMENT_HISTORY_ENTRY)

        val result = extractor.extract(PROVIDED_EHR_PATIENT_RECORD.copy(treatmentHistory = listOf(PROVIDED_TREATMENT_HISTORY)))

        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted).isEqualTo(listOf(EXPECTED_TREATMENT_HISTORY_ENTRY))
    }

    @Test
    fun `Should extract treatment with modifications using curated treatment, curated modification and curated response, stop reason and stop date if configured`() {
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns setOf(
            CURATED_TREATMENT_HISTORY_ENTRY.copy(
                curated = CURATED_TREATMENT_HISTORY_ENTRY.curated?.copy(
                    treatmentHistoryDetails = CURATED_TREATMENT_HISTORY_ENTRY.curated.treatmentHistoryDetails?.copy(
                        stopYear = 2020,
                        stopMonth = 7,
                        stopReason = StopReason.PROGRESSIVE_DISEASE,
                        bestResponse = TreatmentResponse.MIXED
                    )
                )
            )
        )
        every { treatmentCurationDatabase.find(MODIFICATION_NAME) } returns setOf(
            CURATED_TREATMENT_HISTORY_ENTRY.copy(
                curated = EXPECTED_TREATMENT_HISTORY_ENTRY.copy(
                    treatments = setOf(MODIFICATION_TREATMENT)
                )
            )
        )
        val result = extractor.extract(
            PROVIDED_EHR_PATIENT_RECORD.copy(
                treatmentHistory = listOf(PROVIDED_TREATMENT_HISTORY.copy(stopReason = null, response = null, endDate = null))
            )
        )
        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted[0].treatmentHistoryDetails?.stopYear).isEqualTo(2020)
        assertThat(result.extracted[0].treatmentHistoryDetails?.stopMonth).isEqualTo(7)
        assertThat(result.extracted[0].treatmentHistoryDetails?.stopReason).isEqualTo(StopReason.PROGRESSIVE_DISEASE)
        assertThat(result.extracted[0].treatmentHistoryDetails?.bestResponse).isEqualTo(TreatmentResponse.MIXED)
    }

    @Test
    fun `Should fallback to original treatment for modification when no curation available and return a warning`() {
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns setOf(
            CURATED_TREATMENT_HISTORY_ENTRY
        )
        every { treatmentCurationDatabase.find(MODIFICATION_NAME) } returns emptySet()
        val result = extractor.extract(
            PROVIDED_EHR_PATIENT_RECORD.copy(
                treatmentHistory = listOf(PROVIDED_TREATMENT_HISTORY)
            )
        )
        assertThat(result.extracted).containsExactly(
            EXPECTED_TREATMENT_HISTORY_FALLBACK
        )
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
            CURATED_TREATMENT_HISTORY_ENTRY
        )
        every { treatmentCurationDatabase.find(MODIFICATION_NAME) } returns setOf(
            CURATED_TREATMENT_HISTORY_ENTRY.copy(ignore = true)
        )
        val result = extractor.extract(
            PROVIDED_EHR_PATIENT_RECORD.copy(
                treatmentHistory = listOf(PROVIDED_TREATMENT_HISTORY)
            )
        )
        assertThat(result.extracted).containsExactly(EXPECTED_TREATMENT_HISTORY_FALLBACK)
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should throw exception if modification cannot fall back to original treatment`() {
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns setOf(
            CURATED_TREATMENT_HISTORY_ENTRY.copy(curated = EXPECTED_TREATMENT_HISTORY_ENTRY.copy(treatments = emptySet()))
        )
        every { treatmentCurationDatabase.find(MODIFICATION_NAME) } returns emptySet()
        assertThatThrownBy {
            extractor.extract(
                PROVIDED_EHR_PATIENT_RECORD.copy(
                    treatmentHistory = listOf(PROVIDED_TREATMENT_HISTORY)
                )
            )
        }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `Should consider previous conditions as treatment history if not present in non-oncological history curation and is present in oncological history curation`() {
        val curated = CURATED_TREATMENT_HISTORY_ENTRY.curated!!
        every { treatmentCurationDatabase.find(PREVIOUS_CONDITION) } returns setOf(
            CURATED_TREATMENT_HISTORY_ENTRY,
            CURATED_TREATMENT_HISTORY_ENTRY.copy(curated = curated.copy(isTrial = true))
        )
        val result = extractor.extract(
            PROVIDED_EHR_PATIENT_RECORD.copy(
                treatmentHistory = emptyList(),
                priorOtherConditions = listOf(
                    PRIOR_OTHER_PRIOR_CONDITION
                )
            )
        )
        val firstEntry = TreatmentHistoryEntry(
            startYear = PROVIDED_PRIOR_CONDITION_START.year,
            startMonth = PROVIDED_PRIOR_CONDITION_START.monthValue,
            treatments = curated.treatments,
            treatmentHistoryDetails = TreatmentHistoryDetails(
                stopYear = PROVIDED_PRIOR_CONDITION_END.year,
                stopMonth = PROVIDED_PRIOR_CONDITION_END.monthValue,
                stopReason = null,
                bestResponse = null,
                bodyLocations = setOf("bone"),
                bodyLocationCategories = setOf(BodyLocationCategory.BONE),
                switchToTreatments = null,
                cycles = null,
                maintenanceTreatment = TreatmentStage(
                    treatment = TREATMENT,
                    cycles = 1,
                    startYear = CURATED_MODIFICATION_START.year,
                    startMonth = CURATED_TREATMENT_END.monthValue
                ),
            ),
            isTrial = false,
            trialAcronym = "trialAcronym"
        )
        assertThat(result.extracted).containsExactly(
            firstEntry,
            firstEntry.copy(isTrial = true)
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
            PROVIDED_EHR_PATIENT_RECORD.copy(
                treatmentHistory = emptyList(),
                priorOtherConditions = listOf(PRIOR_OTHER_PRIOR_CONDITION)
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
            CURATED_TREATMENT_HISTORY_ENTRY.copy(
                ignore = true,
                input = PREVIOUS_CONDITION
            )
        )
        val result = extractor.extract(
            PROVIDED_EHR_PATIENT_RECORD.copy(
                treatmentHistory = listOf(PROVIDED_TREATMENT_HISTORY.copy(treatmentName = PREVIOUS_CONDITION, modifications = null)),
            )
        )
        assertThat(result.evaluation.treatmentHistoryEntryEvaluatedInputs).containsExactly(PREVIOUS_CONDITION)
    }

    @Test
    fun `Should ignore entries when configured and curated is null`() {
        every { nonOncologicalHistoryCuration.find(TREATMENT_NAME) } returns emptySet()
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns setOf(
            CURATED_TREATMENT_HISTORY_ENTRY.copy(
                ignore = true,
                input = TREATMENT_NAME,
                curated = null
            )
        )
        val result = extractor.extract(
            PROVIDED_EHR_PATIENT_RECORD.copy(
                treatmentHistory = listOf(PROVIDED_TREATMENT_HISTORY),
            )
        )
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.treatmentHistoryEntryEvaluatedInputs).containsExactly(TREATMENT_NAME.lowercase())
    }
}