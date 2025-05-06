package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.config.PriorPrimaryConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.standard.EhrTestData
import com.hartwig.actin.clinical.feed.standard.OTHER_CONDITION_INPUT
import com.hartwig.actin.clinical.feed.standard.TREATMENT_HISTORY_INPUT
import com.hartwig.actin.datamodel.clinical.PriorPrimary
import com.hartwig.actin.datamodel.clinical.TumorStatus
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.actin.datamodel.clinical.provided.ProvidedOtherCondition
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPriorPrimary
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

private val EHR_PRIOR_PRIMARY = ProvidedPriorPrimary(
    tumorLocation = BRAIN_LOCATION,
    tumorType = TYPE,
    status = "Active",
    diagnosisDate = DIAGNOSIS_DATE,
    lastTreatmentDate = DIAGNOSIS_DATE
)

private val EHR_PATIENT_RECORD = EhrTestData.createEhrPatientRecord().copy(
    priorPrimaries = listOf(EHR_PRIOR_PRIMARY)
)

private const val PRIOR_PRIMARY_INPUT = "$BRAIN_LOCATION | $TYPE"
private val UNUSED_DATE = LocalDate.of(2023, 1, 1)
private val EHR_PRIOR_OTHER_CONDITION = ProvidedOtherCondition(name = OTHER_CONDITION_INPUT, startDate = UNUSED_DATE)

private val SECOND_PRIMARY_CONFIG = PriorPrimaryConfig(
    ignore = false,
    input = TREATMENT_HISTORY_INPUT,
    curated = BRAIN_PRIOR_SECOND_PRIMARY
)

private val LUNG_PRIOR_SECOND_PRIMARY = BRAIN_PRIOR_SECOND_PRIMARY.copy(tumorLocation = "lung")

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
        val result = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(result.extracted).containsExactly(
            BRAIN_PRIOR_SECOND_PRIMARY.copy(
                diagnosedMonth = DIAGNOSIS_DATE.monthValue,
                diagnosedYear = DIAGNOSIS_DATE.year,
                lastTreatmentYear = DIAGNOSIS_DATE.year,
                lastTreatmentMonth = DIAGNOSIS_DATE.monthValue
            )
        )
        assertThat(result.evaluation).isEqualTo(CurationExtractionEvaluation(secondPrimaryEvaluatedInputs = setOf(PRIOR_PRIMARY_INPUT)))
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should return curation warning when input not found`() {
        every { priorPrimaryConfigCurationDatabase.find(PRIOR_PRIMARY_INPUT) } returns emptySet()
        val result = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                patientId = "9E9uYbFvpFDjJVCs9XjDGF1LmP8Po6Zb80pYnoBrWg0=",
                category = CurationCategory.SECOND_PRIMARY,
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
        val result = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should curate and extract prior primaries from other conditions, supporting multiple configs per input, ignoring warnings`() {
        every { priorPrimaryConfigCurationDatabase.find(OTHER_CONDITION_INPUT) } returns setOf(
            SECOND_PRIMARY_CONFIG.copy(input = OTHER_CONDITION_INPUT),
            SECOND_PRIMARY_CONFIG.copy(
                input = OTHER_CONDITION_INPUT,
                curated = LUNG_PRIOR_SECOND_PRIMARY
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
        assertThat(result.extracted).containsExactly(BRAIN_PRIOR_SECOND_PRIMARY, LUNG_PRIOR_SECOND_PRIMARY)
        assertThat(result.evaluation).isEqualTo(CurationExtractionEvaluation(secondPrimaryEvaluatedInputs = setOf(OTHER_CONDITION_INPUT)))
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should curate and extract prior primaries from treatment history but ignore curation warnings, supporting multiple configs per input, ignoring warnings`() {
        every { priorPrimaryConfigCurationDatabase.find(TREATMENT_HISTORY_INPUT) } returns setOf(
            SECOND_PRIMARY_CONFIG.copy(input = TREATMENT_HISTORY_INPUT),
            SECOND_PRIMARY_CONFIG.copy(
                input = TREATMENT_HISTORY_INPUT,
                curated = LUNG_PRIOR_SECOND_PRIMARY
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
        assertThat(result.extracted).containsExactly(BRAIN_PRIOR_SECOND_PRIMARY, LUNG_PRIOR_SECOND_PRIMARY)
        assertThat(result.evaluation).isEqualTo(CurationExtractionEvaluation(secondPrimaryEvaluatedInputs = setOf(TREATMENT_HISTORY_INPUT)))
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
        val result = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(result.extracted).containsExactly(
            BRAIN_PRIOR_SECOND_PRIMARY.copy(
                diagnosedMonth = DIAGNOSIS_DATE.monthValue,
                diagnosedYear = DIAGNOSIS_DATE.year,
                lastTreatmentYear = DIAGNOSIS_DATE.year,
                lastTreatmentMonth = DIAGNOSIS_DATE.monthValue
            )
        )
        assertThat(result.evaluation).isEqualTo(CurationExtractionEvaluation(secondPrimaryEvaluatedInputs = setOf(PRIOR_PRIMARY_INPUT)))
        assertThat(result.evaluation.warnings).isEmpty()
    }
}