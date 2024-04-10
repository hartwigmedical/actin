package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.config.ComplicationConfig
import com.hartwig.actin.clinical.datamodel.Complication
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val COMPLICATION_NAME = "complication"
private val EHR_START_DATE = LocalDate.of(2023, 6, 15)
private val CURATED_COMPLICATION = Complication(COMPLICATION_NAME, emptySet(), EHR_START_DATE.year, EHR_START_DATE.monthValue)
private val CURATION_DB = mockk<CurationDatabase<ComplicationConfig>> {
    every { find(any()) }.returns(
        setOf(
            ComplicationConfig(
                "input",
                false,
                false,
                curated = CURATED_COMPLICATION
            )
        )
    )
}

class EhrComplicationExtractorTest {
    @Test
    fun `Should extract complication with end date`() {
        assertExtractionResult(
            EhrComplication(
                COMPLICATION_NAME, emptyList(), EHR_START_DATE, LocalDate.now()
            )
        )
    }

    @Test
    fun `Should extract complication without end date`() {
        assertExtractionResult(
            EhrComplication(
                COMPLICATION_NAME, emptyList(), EHR_START_DATE, null
            )
        )
    }

    fun assertExtractionResult(ehrComplication: EhrComplication) {
        val ehrPatientRecord = EhrTestData.createEhrPatientRecord().copy(complications = listOf(ehrComplication))
        val result = EhrComplicationExtractor(CURATION_DB).extract(ehrPatientRecord)
        assertThat(result.extracted.size).isEqualTo(1)
        val extracted = result.extracted[0]
        assertThat(extracted.name).isEqualTo("complication")
        assertThat(extracted.year).isEqualTo(EHR_START_DATE.year)
        assertThat(extracted.month).isEqualTo(EHR_START_DATE.monthValue)
    }
}