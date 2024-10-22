package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.config.SurgeryConfig
import com.hartwig.actin.clinical.feed.emc.TestFeedFactory
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

private const val SURGERY_NAME = "surgery_name"
private val START_DATE = LocalDate.of(2024, 10, 21)
private val END_DATE = LocalDate.of(2024, 10, 22)

private val SURGERY_ENTRY = TestFeedFactory.surgeryEntry(START_DATE, END_DATE)

private const val PATIENT = "patient"

class SurgeryExtractorTest {

    val curationDatabase = mockk<CurationDatabase<SurgeryConfig>>()
    val extractor = SurgeryExtractor(curationDatabase)

    @Test
    fun `Should curate surgery name and take end date and status from feed`() {
        every { curationDatabase.find(SURGERY_ENTRY.codeCodingDisplayOriginal) } returns setOf(
            SurgeryConfig(
                input = SURGERY_ENTRY.codeCodingDisplayOriginal,
                name = SURGERY_NAME
            )
        )
        val result =
            extractor.extract(PATIENT, listOf(SURGERY_ENTRY))
        assertThat(result.extracted).containsOnly(
            Surgery(
                SURGERY_NAME,
                END_DATE,
                SurgeryStatus.valueOf(SURGERY_ENTRY.procedureStatus.uppercase())
            )
        )
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should warn when surgery name is not curated or ignored`() {
        every { curationDatabase.find(SURGERY_ENTRY.codeCodingDisplayOriginal) } returns emptySet()
        val result =
            extractor.extract(PATIENT, listOf(SURGERY_ENTRY))
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT,
                CurationCategory.SURGERY,
                SURGERY_ENTRY.codeCodingDisplayOriginal,
                "Could not find surgery config for input 'diagnostics'"
            )
        )
    }

    @Test
    fun `Should throw illegal state when end date or status are configured to be curated`() {
        every { curationDatabase.find(SURGERY_ENTRY.codeCodingDisplayOriginal) } returns setOf(
            SurgeryConfig(
                input = SURGERY_ENTRY.codeCodingDisplayOriginal,
                name = SURGERY_NAME,
                endDate = END_DATE,
                status = SurgeryStatus.CANCELLED
            )
        )
        assertThatThrownBy { extractor.extract(PATIENT, listOf(SURGERY_ENTRY)) }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `Should ignore surgeries when curation configured to ignore`() {
        every { curationDatabase.find(SURGERY_ENTRY.codeCodingDisplayOriginal) } returns setOf(
            SurgeryConfig(
                input = SURGERY_ENTRY.codeCodingDisplayOriginal,
                ignore = true,
                name = "<ignore>"
            )
        )
        val result =
            extractor.extract(PATIENT, listOf(SURGERY_ENTRY))
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).isEmpty()
    }
}