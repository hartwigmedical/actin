package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.feed.standard.FeedTestData
import com.hartwig.actin.clinical.feed.standard.FeedTestData.FEED_PATIENT_RECORD
import com.hartwig.actin.clinical.feed.standard.HASHED_ID_IN_BASE64
import com.hartwig.actin.clinical.feed.standard.TREATMENT_NAME
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentStage
import com.hartwig.feed.datamodel.DatedEntry
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val TREATMENT_START = LocalDate.of(2021, 4, 1)
private val TREATMENT_END = LocalDate.of(2021, 5, 1)
private val FEED_TREATMENT_HISTORY = FeedTestData.FEED_TREATMENT_HISTORY
    .copy(name = TREATMENT_NAME, startDate = TREATMENT_START, endDate = TREATMENT_END)

private const val OTHER_CONDITION_NAME = "other_condition"
private val OTHER_CONDITION_START = LocalDate.of(2022, 7, 1)
private val OTHER_CONDITION_END = LocalDate.of(2022, 8, 1)
private val OTHER_CONDITION = DatedEntry(
    name = OTHER_CONDITION_NAME,
    startDate = OTHER_CONDITION_START,
    endDate = OTHER_CONDITION_END,
)
private val TREATMENT = DrugTreatment("drug", drugs = emptySet())

private val CURATED_TREATMENT_STAGE = TreatmentStage(
    treatment = TREATMENT,
    cycles = 1,
    startYear = LocalDate.of(2020, 2, 1).year,
    startMonth = LocalDate.of(2020, 2, 1).monthValue
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

private val EXPECTED_TREATMENT_HISTORY_FROM_PRIOR_OTHER_CONDITION = TreatmentHistoryEntry(
    startYear = OTHER_CONDITION_START.year,
    startMonth = OTHER_CONDITION_START.monthValue,
    treatments = CURATED_TREATMENT_HISTORY_ENTRY.curated!!.treatments,
    treatmentHistoryDetails = TreatmentHistoryDetails(
        stopYear = OTHER_CONDITION_END.year,
        stopMonth = OTHER_CONDITION_END.monthValue,
        stopReason = null,
        bestResponse = null,
        bodyLocations = setOf("bone"),
        bodyLocationCategories = setOf(BodyLocationCategory.BONE),
        switchToTreatments = null,
        cycles = null,
        maintenanceTreatment = CURATED_TREATMENT_STAGE,
    ),
    isTrial = false,
    trialAcronym = "trialAcronym",
    intents = setOf(Intent.PALLIATIVE)
)

class StandardOncologicalHistoryExtractorTest {

    private val treatmentCurationDatabase = mockk<CurationDatabase<TreatmentHistoryEntryConfig>>()
    private val extractor = StandardOncologicalHistoryExtractor(treatmentCurationDatabase)

    @Test
    fun `Should filter treatment history entry and warn when no curation for treatment name`() {
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns emptySet()
        val result = extractor.extract(
            FEED_PATIENT_RECORD.copy(treatmentHistory = listOf(FEED_TREATMENT_HISTORY))
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
    fun `Should extract treatment with modifications`() {
        val modificationTreatmentStage = TreatmentStage(
            DrugTreatment("modification", drugs = emptySet()), 2, 2021, 6
        )

        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns setOf(
            TreatmentHistoryEntryConfig(
                TREATMENT_NAME,
                false,
                CURATED_TREATMENT_HISTORY_ENTRY.curated?.let { entry ->
                    entry.copy(
                        treatmentHistoryDetails = entry.treatmentHistoryDetails?.copy(
                            switchToTreatments = listOf(modificationTreatmentStage)
                        )
                    )
                }
            )
        )

        val result = extractor.extract(FEED_PATIENT_RECORD.copy(treatmentHistory = listOf(FEED_TREATMENT_HISTORY)))

        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted).containsExactly(
            TreatmentHistoryEntry(
                startYear = TREATMENT_START.year,
                startMonth = TREATMENT_START.monthValue,
                treatments = CURATED_TREATMENT_HISTORY_ENTRY.curated!!.treatments,
                intents = setOf(Intent.PALLIATIVE),
                treatmentHistoryDetails = TreatmentHistoryDetails(
                    stopYear = TREATMENT_END.year,
                    stopMonth = TREATMENT_END.monthValue,
                    bodyLocations = setOf("bone"),
                    bodyLocationCategories = setOf(BodyLocationCategory.BONE),
                    switchToTreatments = listOf(modificationTreatmentStage),
                    maintenanceTreatment = CURATED_TREATMENT_STAGE,
                ),
                isTrial = false,
                trialAcronym = "trialAcronym",
            )
        )
    }

    @Test
    fun `Should merge treatments by taking the other condition when the same treatment and date exists in both the onco history curation and prior other condition curation`() {
        val duplicatedCuration =
            CURATED_TREATMENT_HISTORY_ENTRY.copy(curated = CURATED_TREATMENT_HISTORY_ENTRY.curated?.copy(startYear = 2024, startMonth = 10))
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns setOf(duplicatedCuration)
        every { treatmentCurationDatabase.find(OTHER_CONDITION_NAME) } returns setOf(duplicatedCuration)

        val result = extractor.extract(
            FEED_PATIENT_RECORD.copy(
                treatmentHistory = listOf(FEED_TREATMENT_HISTORY), otherConditions = listOf(OTHER_CONDITION)
            )
        )

        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted).containsOnly(
            EXPECTED_TREATMENT_HISTORY_FROM_PRIOR_OTHER_CONDITION.copy(
                startYear = 2024,
                startMonth = 10
            )
        )
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
        val result = extractor.extract(
            FEED_PATIENT_RECORD.copy(treatmentHistory = listOf(FEED_TREATMENT_HISTORY))
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
    fun `Should consider previous conditions as treatment history if not present in non-oncological history curation and is present in oncological history curation`() {
        val curated = CURATED_TREATMENT_HISTORY_ENTRY.curated!!
        every { treatmentCurationDatabase.find(OTHER_CONDITION_NAME) } returns setOf(
            CURATED_TREATMENT_HISTORY_ENTRY,
            CURATED_TREATMENT_HISTORY_ENTRY.copy(curated = curated.copy(isTrial = true))
        )
        val result = extractor.extract(
            FEED_PATIENT_RECORD.copy(
                treatmentHistory = emptyList(),
                otherConditions = listOf(OTHER_CONDITION)
            )
        )
        assertThat(result.extracted).containsExactly(
            EXPECTED_TREATMENT_HISTORY_FROM_PRIOR_OTHER_CONDITION,
            EXPECTED_TREATMENT_HISTORY_FROM_PRIOR_OTHER_CONDITION.copy(isTrial = true)
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
            FEED_PATIENT_RECORD.copy(
                treatmentHistory = listOf(FEED_TREATMENT_HISTORY),
            )
        )
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.treatmentHistoryEntryEvaluatedInputs).containsExactly(TREATMENT_NAME.lowercase())
    }
}