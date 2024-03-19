package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.curation.CurationDatabase
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
    diagnosedYear = 2024,
    diagnosedMonth = 2,
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

class EhrPriorPrimariesExtractorTest {

    private val secondPrimaryConfigCurationDatabase = mockk<CurationDatabase<SecondPrimaryConfig>>()
    private val extractor = EhrPriorPrimariesExtractor(secondPrimaryConfigCurationDatabase)

    @Test
    fun `Should extract and curate prior primary`() {
        val ehrPatientRecord = mockk<EhrPatientRecord>()
        val input = "location | type"
        every { secondPrimaryConfigCurationDatabase.find(input) } returns setOf(
            SecondPrimaryConfig(
                ignore = false,
                input = input,
                curated = PRIOR_SECOND_PRIMARY
            )
        )
        every { ehrPatientRecord.priorPrimaries } returns listOf(
            EHR_PRIOR_PRIMARY
        )
        val result = extractor.extract(ehrPatientRecord)
        assertThat(result.extracted).containsExactly(PRIOR_SECOND_PRIMARY)
        assertThat(result.evaluation).isEqualTo(CurationExtractionEvaluation())
    }

    @Test
    fun `Should default tumor status to UNKNOWN when null`() {
        val ehrPatientRecord = mockk<EhrPatientRecord>()
        every { ehrPatientRecord.priorPrimaries } returns listOf(
            EHR_PRIOR_PRIMARY.copy(status = null)
        )
        val result = extractor.extract(ehrPatientRecord)
        assertThat(result.extracted).containsExactly(
            PRIOR_SECOND_PRIMARY.copy(status = TumorStatus.UNKNOWN)
        )
        assertThat(result.evaluation).isEqualTo(CurationExtractionEvaluation())
    }

}