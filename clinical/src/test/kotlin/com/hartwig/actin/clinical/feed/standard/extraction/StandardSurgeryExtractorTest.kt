package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.actin.clinical.curation.config.SurgeryNameConfig
import com.hartwig.actin.clinical.feed.standard.FeedTestData
import com.hartwig.actin.clinical.feed.standard.FeedTestData.FEED_PATIENT_RECORD
import com.hartwig.actin.clinical.feed.standard.HASHED_ID_IN_BASE64
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PROVIDED_SURGERY_NAME = "surgery one"
private const val CURATED_SURGERY_NAME = "surgery 1"

private val PROVIDED_SURGERY_WITH_NAME = FeedTestData.createFeedSurgery(PROVIDED_SURGERY_NAME)

class StandardSurgeryExtractorTest {

    private val surgeryNameCuration = mockk<CurationDatabase<SurgeryNameConfig>>()
    private val extractor = StandardSurgeryExtractor(surgeryNameCuration)

    @Test
    fun `Should filter surgery entry and warn when no curation for surgery name`() {
        every { surgeryNameCuration.find(PROVIDED_SURGERY_NAME) } returns emptySet()
        val result = extractor.extract(
            FEED_PATIENT_RECORD.copy(surgeries = listOf(PROVIDED_SURGERY_WITH_NAME))
        )
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                message = "Could not find surgery config for input 'surgery one'",
                patientId = HASHED_ID_IN_BASE64,
                category = CurationCategory.SURGERY_NAME,
                feedInput = PROVIDED_SURGERY_NAME
            )
        )
    }

    @Test
    fun `Should filter surgery entry when surgery is set to be ignore in curation`() {
        val ignoredName = "Geen ingreep- operatie uitgesteld"


        every { surgeryNameCuration.find(PROVIDED_SURGERY_NAME) } returns setOf(
            SurgeryNameConfig(input = PROVIDED_SURGERY_NAME, ignore = false, name = CURATED_SURGERY_NAME)
        )
        every { surgeryNameCuration.find(ignoredName) } returns setOf(
            SurgeryNameConfig(input = ignoredName, ignore = true, name = "<ignore>")
        )

        val result = extractor.extract(
            FEED_PATIENT_RECORD.copy(
                surgeries = listOf(PROVIDED_SURGERY_WITH_NAME, FeedTestData.createFeedSurgery(ignoredName))
            )
        )
        assertThat(result.extracted).isNotEmpty()
        assertThat(result.extracted.size).isEqualTo(1)
        assertThat(result.extracted).containsExactly(
            Surgery(
                name = CURATED_SURGERY_NAME,
                endDate = PROVIDED_SURGERY_WITH_NAME.endDate,
                status = SurgeryStatus.FINISHED
            )
        )
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should return surgery entry when surgery name is null without curation`() {
        val surgery = FeedTestData.createFeedSurgery(null)
        val result = extractor.extract(FEED_PATIENT_RECORD.copy(surgeries = listOf(surgery)))

        assertThat(result.extracted).containsExactly(
            Surgery(name = surgery.name, endDate = surgery.endDate, status = SurgeryStatus.FINISHED)
        )
        assertThat(result.evaluation.warnings).isEmpty()
    }
}