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

private val EHR_PRIOR_PRIMARY = EhrPriorPrimary(
    tumorLocation = LOCATION,
    tumorType = TYPE,
    status = "ACTIVE",
    diagnosisDate = LocalDate.of(2024, 2, 23),
    statusDate = LocalDate.of(2024, 2, 23)
)

private val EHR_PATIENT_RECORD = EhrTestData.createEhrPatientRecord().copy(
    priorPrimaries = listOf(EHR_PRIOR_PRIMARY)
)

private const val INPUT = "location | type"

class EhrPriorPrimariesExtractorTest {

    private val secondPrimaryConfigCurationDatabase = mockk<CurationDatabase<SecondPrimaryConfig>>()
    private val extractor = EhrPriorPrimariesExtractor(secondPrimaryConfigCurationDatabase)

    @Test
    fun `Should curate and extract prior primary`() {
        every { secondPrimaryConfigCurationDatabase.find(INPUT) } returns setOf(
            SecondPrimaryConfig(
                ignore = false,
                input = INPUT,
                curated = PRIOR_SECOND_PRIMARY
            )
        )
        val result = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(result.extracted).containsExactly(PRIOR_SECOND_PRIMARY.copy(diagnosedMonth = 2, diagnosedYear = 2024))
        assertThat(result.evaluation).isEqualTo(CurationExtractionEvaluation(secondPrimaryEvaluatedInputs = setOf(INPUT)))
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should return curation warning when input not found`() {
        every { secondPrimaryConfigCurationDatabase.find(INPUT) } returns emptySet()
        val result = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                patientId = "9E9uYbFvpFDjJVCs9XjDGF1LmP8Po6Zb80pYnoBrWg0=",
                category = CurationCategory.SECOND_PRIMARY,
                feedInput = INPUT,
                message = "Could not find prior primary config for input 'location | type'"
            )
        )
    }

    @Test
    fun `Should ignore primaries when configured in curation`() {
        every { secondPrimaryConfigCurationDatabase.find(INPUT) } returns setOf(
            SecondPrimaryConfig(
                ignore = true,
                input = INPUT
            )
        )
        val result = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).isEmpty()
    }
}