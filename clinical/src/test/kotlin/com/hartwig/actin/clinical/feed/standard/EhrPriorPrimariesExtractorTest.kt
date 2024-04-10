package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.TumorStatus
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val LOCATION = "location"
private const val TYPE = "type"

private val PRIOR_SECOND_PRIMARY = PriorSecondPrimary(
    tumorLocation = LOCATION,
    tumorType = TYPE,
    status = TumorStatus.ACTIVE,
    diagnosedYear = null,
    diagnosedMonth = null,
    tumorSubLocation = "",
    tumorSubType = "",
    treatmentHistory = ""
)

private val DIAGNOSIS_DATE = LocalDate.of(2024, 2, 23)

private val EHR_PRIOR_PRIMARY = EhrPriorPrimary(
    tumorLocation = LOCATION,
    tumorType = TYPE,
    status = "ACTIVE",
    diagnosisDate = DIAGNOSIS_DATE,
    statusDate = DIAGNOSIS_DATE
)

private val EHR_PATIENT_RECORD = EhrTestData.createEhrPatientRecord().copy(
    priorPrimaries = listOf(EHR_PRIOR_PRIMARY)
)

private const val PRIOR_PRIMARY_INPUT = "location | type"
private val UNUSED_DATE = LocalDate.of(2023, 1, 1)
private val EHR_PRIOR_OTHER_CONDITION = EhrPriorOtherCondition(name = PRIOR_CONDITION_INPUT, startDate = UNUSED_DATE)

class EhrPriorPrimariesExtractorTest {

    private val secondPrimaryConfigCurationDatabase = mockk<CurationDatabase<SecondPrimaryConfig>> {
        every { find(any()) } returns emptySet()
    }
    private val extractor = EhrPriorPrimariesExtractor(secondPrimaryConfigCurationDatabase)

    @Test
    fun `Should curate and extract prior primary`() {
        every { secondPrimaryConfigCurationDatabase.find(PRIOR_PRIMARY_INPUT) } returns setOf(
            SecondPrimaryConfig(
                ignore = false,
                input = PRIOR_PRIMARY_INPUT,
                curated = PRIOR_SECOND_PRIMARY
            )
        )
        val result = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(result.extracted).containsExactly(
            PRIOR_SECOND_PRIMARY.copy(
                diagnosedMonth = DIAGNOSIS_DATE.monthValue,
                diagnosedYear = DIAGNOSIS_DATE.year
            )
        )
        assertThat(result.evaluation).isEqualTo(CurationExtractionEvaluation(secondPrimaryEvaluatedInputs = setOf(PRIOR_PRIMARY_INPUT)))
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should return curation warning when input not found`() {
        every { secondPrimaryConfigCurationDatabase.find(PRIOR_PRIMARY_INPUT) } returns emptySet()
        val result = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                patientId = "9E9uYbFvpFDjJVCs9XjDGF1LmP8Po6Zb80pYnoBrWg0=",
                category = CurationCategory.SECOND_PRIMARY,
                feedInput = PRIOR_PRIMARY_INPUT,
                message = "Could not find prior primary config for input 'location | type'"
            )
        )
    }

    @Test
    fun `Should ignore primaries when configured in curation`() {
        every { secondPrimaryConfigCurationDatabase.find(PRIOR_PRIMARY_INPUT) } returns setOf(
            SecondPrimaryConfig(
                ignore = true,
                input = PRIOR_PRIMARY_INPUT
            )
        )
        val result = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should curate and extract prior primaries from previous conditions `() {
        every { secondPrimaryConfigCurationDatabase.find(PRIOR_CONDITION_INPUT) } returns setOf(
            SecondPrimaryConfig(
                ignore = false,
                input = PRIOR_CONDITION_INPUT,
                curated = PRIOR_SECOND_PRIMARY
            )
        )
        val result = extractor.extract(
            EHR_PATIENT_RECORD.copy(
                priorPrimaries = emptyList(),
                priorOtherConditions = listOf(
                    EHR_PRIOR_OTHER_CONDITION,
                    EHR_PRIOR_OTHER_CONDITION.copy(name = "another prior condition")
                )
            )
        )
        assertThat(result.extracted).containsExactly(PRIOR_SECOND_PRIMARY)
        assertThat(result.evaluation).isEqualTo(CurationExtractionEvaluation(secondPrimaryEvaluatedInputs = setOf(PRIOR_CONDITION_INPUT)))
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should curate and extract prior primaries from treatment history but ignore curation warnings`() {
        every { secondPrimaryConfigCurationDatabase.find(TREATMENT_HISTORY_INPUT) } returns setOf(
            SecondPrimaryConfig(
                ignore = false,
                input = TREATMENT_HISTORY_INPUT,
                curated = PRIOR_SECOND_PRIMARY
            )
        )
        val result = extractor.extract(
            EHR_PATIENT_RECORD.copy(
                priorPrimaries = emptyList(),
                treatmentHistory = listOf(
                    EhrTestData.createEhrTreatmentHistory().copy(treatmentName = TREATMENT_HISTORY_INPUT),
                    EhrTestData.createEhrTreatmentHistory().copy(treatmentName = "another treatment")
                )
            )
        )
        assertThat(result.extracted).containsExactly(PRIOR_SECOND_PRIMARY)
        assertThat(result.evaluation).isEqualTo(CurationExtractionEvaluation(secondPrimaryEvaluatedInputs = setOf(TREATMENT_HISTORY_INPUT)))
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should always use diagnosis year and month from feed, even when in curation data `() {
        every { secondPrimaryConfigCurationDatabase.find(PRIOR_PRIMARY_INPUT) } returns setOf(
            SecondPrimaryConfig(
                ignore = false,
                input = PRIOR_PRIMARY_INPUT,
                curated = PRIOR_SECOND_PRIMARY.copy(diagnosedYear = 2023, diagnosedMonth = 1)
            )
        )
        val result = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(result.extracted).containsExactly(
            PRIOR_SECOND_PRIMARY.copy(
                diagnosedMonth = DIAGNOSIS_DATE.monthValue,
                diagnosedYear = DIAGNOSIS_DATE.year
            )
        )
        assertThat(result.evaluation).isEqualTo(CurationExtractionEvaluation(secondPrimaryEvaluatedInputs = setOf(PRIOR_PRIMARY_INPUT)))
        assertThat(result.evaluation.warnings).isEmpty()
    }
}