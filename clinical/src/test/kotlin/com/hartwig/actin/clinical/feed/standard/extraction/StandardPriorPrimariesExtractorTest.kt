package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.config.PriorPrimaryConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.standard.FeedTestData
import com.hartwig.actin.datamodel.clinical.PriorPrimary
import com.hartwig.actin.datamodel.clinical.TumorStatus
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.feed.datamodel.DatedEntry
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val BRAIN_LOCATION = "brain"
private const val TYPE = "type"

private val BRAIN_PRIOR_SECOND_PRIMARY = PriorPrimary(
    tumorLocation = BRAIN_LOCATION,
    tumorType = TYPE,
    status = TumorStatus.ACTIVE,
    diagnosedYear = null,
    diagnosedMonth = null,
    tumorSubLocation = "",
    tumorSubType = "",
    treatmentHistory = ""
)

private val DIAGNOSIS_DATE = LocalDate.of(2024, 2, 23)
private val LAST_TREATMENT_DATE = LocalDate.of(2024, 10, 23)

private const val PRIOR_PRIMARY_INPUT = "$BRAIN_LOCATION | $TYPE"

private val PATIENT_RECORD = FeedTestData.FEED_PATIENT_RECORD.copy(
    priorPrimaries = listOf(DatedEntry(name = PRIOR_PRIMARY_INPUT, startDate = DIAGNOSIS_DATE, endDate = LAST_TREATMENT_DATE))
)

class StandardPriorPrimariesExtractorTest {

    private val priorPrimaryConfigCurationDatabase = mockk<CurationDatabase<PriorPrimaryConfig>> {
        every { find(any()) } returns emptySet()
    }
    private val extractor = StandardPriorPrimariesExtractor(priorPrimaryConfigCurationDatabase)

    @Test
    fun `Should curate and extract prior primary`() {
        every { priorPrimaryConfigCurationDatabase.find(PRIOR_PRIMARY_INPUT) } returns setOf(
            PriorPrimaryConfig(
                ignore = false,
                input = PRIOR_PRIMARY_INPUT,
                curated = BRAIN_PRIOR_SECOND_PRIMARY
            )
        )
        val result = extractor.extract(PATIENT_RECORD)
        assertThat(result.extracted).containsExactly(
            BRAIN_PRIOR_SECOND_PRIMARY.copy(
                diagnosedMonth = DIAGNOSIS_DATE.monthValue,
                diagnosedYear = DIAGNOSIS_DATE.year,
                lastTreatmentYear = LAST_TREATMENT_DATE.year,
                lastTreatmentMonth = LAST_TREATMENT_DATE.monthValue
            )
        )
        assertThat(result.evaluation).isEqualTo(CurationExtractionEvaluation(priorPrimaryEvaluatedInputs = setOf(PRIOR_PRIMARY_INPUT)))
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should curate and extract multiple prior primaries for one input when defined`() {
        val input = "brain | type | prior primaries in kidney and liver"
        val kidneyPrior = PriorPrimary("kidney", "", "Carcinoma", "", treatmentHistory = "", status = TumorStatus.INACTIVE)
        val liverPrior = kidneyPrior.copy(tumorLocation = "liver")

        every { priorPrimaryConfigCurationDatabase.find(input) } returns setOf(
            PriorPrimaryConfig(ignore = false, input = input, curated = kidneyPrior),
            PriorPrimaryConfig(ignore = false, input = input, curated = liverPrior)
        )
        val patientWithMultiplePrimaries = FeedTestData.FEED_PATIENT_RECORD.copy(
            priorPrimaries = listOf(DatedEntry(name = input, startDate = DIAGNOSIS_DATE, endDate = LAST_TREATMENT_DATE))
        )

        val result = extractor.extract(patientWithMultiplePrimaries)
        val expectedKidneyPrior = kidneyPrior.copy(
            diagnosedMonth = DIAGNOSIS_DATE.monthValue,
            diagnosedYear = DIAGNOSIS_DATE.year,
            lastTreatmentYear = LAST_TREATMENT_DATE.year,
            lastTreatmentMonth = LAST_TREATMENT_DATE.monthValue
        )
        val expectedLiverPrior = expectedKidneyPrior.copy(tumorLocation = "liver")
        assertThat(result.extracted).containsExactly(expectedKidneyPrior, expectedLiverPrior)
        assertThat(result.evaluation).isEqualTo(CurationExtractionEvaluation(priorPrimaryEvaluatedInputs = setOf(input)))
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should return curation warning when input not found`() {
        every { priorPrimaryConfigCurationDatabase.find(PRIOR_PRIMARY_INPUT) } returns emptySet()
        val result = extractor.extract(PATIENT_RECORD)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                patientId = "9E9uYbFvpFDjJVCs9XjDGF1LmP8Po6Zb80pYnoBrWg0=",
                category = CurationCategory.PRIOR_PRIMARY,
                feedInput = PRIOR_PRIMARY_INPUT,
                message = "Could not find prior primary config for input '$PRIOR_PRIMARY_INPUT'"
            )
        )
    }

    @Test
    fun `Should ignore primaries when configured in curation`() {
        every { priorPrimaryConfigCurationDatabase.find(PRIOR_PRIMARY_INPUT) } returns setOf(
            PriorPrimaryConfig(
                ignore = true,
                input = PRIOR_PRIMARY_INPUT
            )
        )
        val result = extractor.extract(PATIENT_RECORD)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should always use diagnosis year and month from feed, even when in curation data `() {
        every { priorPrimaryConfigCurationDatabase.find(PRIOR_PRIMARY_INPUT) } returns setOf(
            PriorPrimaryConfig(
                ignore = false,
                input = PRIOR_PRIMARY_INPUT,
                curated = BRAIN_PRIOR_SECOND_PRIMARY.copy(diagnosedYear = 2023, diagnosedMonth = 1)
            )
        )
        val result = extractor.extract(PATIENT_RECORD)
        assertThat(result.extracted).containsExactly(
            BRAIN_PRIOR_SECOND_PRIMARY.copy(
                diagnosedMonth = DIAGNOSIS_DATE.monthValue,
                diagnosedYear = DIAGNOSIS_DATE.year,
                lastTreatmentYear = LAST_TREATMENT_DATE.year,
                lastTreatmentMonth = LAST_TREATMENT_DATE.monthValue
            )
        )
        assertThat(result.evaluation).isEqualTo(CurationExtractionEvaluation(priorPrimaryEvaluatedInputs = setOf(PRIOR_PRIMARY_INPUT)))
        assertThat(result.evaluation.warnings).isEmpty()
    }
}