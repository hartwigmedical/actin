package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.feed.standard.EhrTestData
import com.hartwig.actin.clinical.feed.standard.EhrTestData.createEhrPatientRecord
import com.hartwig.actin.clinical.feed.standard.HASHED_ID_IN_BASE64
import com.hartwig.actin.clinical.feed.standard.MODIFICATION_NAME
import com.hartwig.actin.clinical.feed.standard.ProvidedPriorOtherCondition
import com.hartwig.actin.clinical.feed.standard.TREATMENT_NAME
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentStage
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

private const val PRIOR_CONDITION = "prior_condition"
private val PROVIDED_PRIOR_CONDITION_START = LocalDate.of(2022, 7, 1)
private val PROVIDED_PRIOR_CONDITION_END = LocalDate.of(2022, 8, 1)
private val PRIOR_OTHER_CONDITION = ProvidedPriorOtherCondition(
    name = PRIOR_CONDITION,
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
        intents = setOf(Intent.PALLIATIVE),
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
    intents = setOf(Intent.PALLIATIVE),
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
    private val extractor = StandardOncologicalHistoryExtractor(treatmentCurationDatabase)

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
    fun `Should extract treatment with modifications using curated treatment, curated modification and provided response, stop reason, stop date`() {
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns setOf(CURATED_TREATMENT_HISTORY_ENTRY)
        every { treatmentCurationDatabase.find(MODIFICATION_NAME) } returns setOf(CURATED_MODIFICATION_TREATMENT_HISTORY_ENTRY)

        val result = extractor.extract(PROVIDED_EHR_PATIENT_RECORD.copy(treatmentHistory = listOf(PROVIDED_TREATMENT_HISTORY)))

        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted).isEqualTo(listOf(EXPECTED_TREATMENT_HISTORY_ENTRY))
    }

    @Test
    fun `Should extract treatment with modifications using curated intent when provided`() {
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns setOf(
            CURATED_TREATMENT_HISTORY_ENTRY.copy(
                curated = CURATED_TREATMENT_HISTORY_ENTRY.curated?.copy(
                    intents = setOf(Intent.ADJUVANT)
                )
            )
        )
        every { treatmentCurationDatabase.find(MODIFICATION_NAME) } returns setOf(CURATED_MODIFICATION_TREATMENT_HISTORY_ENTRY)

        val result = extractor.extract(PROVIDED_EHR_PATIENT_RECORD.copy(treatmentHistory = listOf(PROVIDED_TREATMENT_HISTORY)))

        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted).isEqualTo(listOf(EXPECTED_TREATMENT_HISTORY_ENTRY.copy(intents = setOf(Intent.ADJUVANT))))
    }

    @Test
    fun `Should merge treatments by taking the oncological history when the same treatment and date exists in both the onco history curation and prior other condition curation`() {
        val duplicatedCuration =
            CURATED_TREATMENT_HISTORY_ENTRY.copy(curated = CURATED_TREATMENT_HISTORY_ENTRY.curated?.copy(startYear = 2024, startMonth = 10))
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns setOf(duplicatedCuration)
        every { treatmentCurationDatabase.find(PRIOR_CONDITION) } returns setOf(duplicatedCuration)
        every { treatmentCurationDatabase.find(MODIFICATION_NAME) } returns setOf(CURATED_MODIFICATION_TREATMENT_HISTORY_ENTRY)

        val result = extractor.extract(
            PROVIDED_EHR_PATIENT_RECORD.copy(
                treatmentHistory = listOf(PROVIDED_TREATMENT_HISTORY), priorOtherConditions = listOf(
                    PRIOR_OTHER_CONDITION
                )
            )
        )

        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted).containsOnly(EXPECTED_TREATMENT_HISTORY_ENTRY.copy(startYear = 2024, startMonth = 10))
    }

    @Test
    fun `Should extract treatment with modifications using curated treatment, curated modification and curated response, start, stop reason and start, stop date if configured`() {
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns setOf(
            CURATED_TREATMENT_HISTORY_ENTRY.copy(
                curated = CURATED_TREATMENT_HISTORY_ENTRY.curated?.copy(
                    startYear = 2019,
                    startMonth = 6,
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
        assertThat(result.extracted[0].startYear).isEqualTo(2019)
        assertThat(result.extracted[0].startMonth).isEqualTo(6)
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
        every { treatmentCurationDatabase.find(PRIOR_CONDITION) } returns setOf(
            CURATED_TREATMENT_HISTORY_ENTRY,
            CURATED_TREATMENT_HISTORY_ENTRY.copy(curated = curated.copy(isTrial = true))
        )
        val result = extractor.extract(
            PROVIDED_EHR_PATIENT_RECORD.copy(
                treatmentHistory = emptyList(),
                priorOtherConditions = listOf(
                    PRIOR_OTHER_CONDITION
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
            trialAcronym = "trialAcronym",
            intents = setOf(Intent.PALLIATIVE)
        )
        assertThat(result.extracted).containsExactly(
            firstEntry,
            firstEntry.copy(isTrial = true)
        )
    }

    @Test
    fun `Should ignore entries when configured and curated is null`() {
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