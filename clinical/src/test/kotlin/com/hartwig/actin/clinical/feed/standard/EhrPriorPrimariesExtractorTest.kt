package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.TumorStatus
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class EhrPriorPrimariesExtractorTest {

    @Test
    fun `Should extract prior primary`() {
        val extractor = EhrPriorPrimariesExtractor()
        val ehrPatientRecord = mockk<EhrPatientRecord>()
        every { ehrPatientRecord.priorPrimaries } returns listOf(
            ehrPriorPrimary()
        )
        val result = extractor.extract(ehrPatientRecord)
        assertThat(result.extracted).containsExactly(
            priorSecondPrimary()
        )
        assertThat(result.evaluation).isEqualTo(CurationExtractionEvaluation())
    }

    @Test
    fun `Should default tumor status to UNKNOWN when null`() {
        val extractor = EhrPriorPrimariesExtractor()
        val ehrPatientRecord = mockk<EhrPatientRecord>()
        every { ehrPatientRecord.priorPrimaries } returns listOf(
            ehrPriorPrimary().copy(status = null)
        )
        val result = extractor.extract(ehrPatientRecord)
        assertThat(result.extracted).containsExactly(
            priorSecondPrimary().copy(status = TumorStatus.UNKNOWN)
        )
        assertThat(result.evaluation).isEqualTo(CurationExtractionEvaluation())
    }

    private fun ehrPriorPrimary() = EhrPriorPrimary(
        tumorLocation = "location",
        tumorType = "type",
        status = "ACTIVE",
        diagnosisDate = LocalDate.of(2024, 2, 23),
        statusDate = LocalDate.of(2024, 2, 23)
    )

    private fun priorSecondPrimary() = PriorSecondPrimary(
        tumorLocation = "location",
        tumorType = "type",
        status = TumorStatus.ACTIVE,
        diagnosedYear = 2024,
        diagnosedMonth = 2,
        tumorSubLocation = "",
        tumorSubType = "",
        treatmentHistory = ""
    )
}