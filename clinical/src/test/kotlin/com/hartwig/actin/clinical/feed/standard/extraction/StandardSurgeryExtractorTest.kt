package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.actin.clinical.curation.config.SurgeryConfig
import com.hartwig.actin.clinical.feed.standard.FeedTestData
import com.hartwig.actin.clinical.feed.standard.FeedTestData.FEED_PATIENT_RECORD
import com.hartwig.actin.clinical.feed.standard.HASHED_ID_IN_BASE64
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PROVIDED_SURGERY_NAME = "surgery one"
private const val CURATED_SURGERY_NAME = "surgery 1"

private val PROVIDED_SURGERY_WITH_NAME = FeedTestData.createFeedSurgery(PROVIDED_SURGERY_NAME)

class StandardSurgeryExtractorTest {

    private val surgeryCuration = mockk<CurationDatabase<SurgeryConfig>>()
    private val extractor = StandardSurgeryExtractor(surgeryCuration)

    @Test
    fun `Should filter surgery entry and warn when no curation for surgery`() {
        every { surgeryCuration.find(PROVIDED_SURGERY_NAME) } returns emptySet()
        val result = extractor.extract(
            FEED_PATIENT_RECORD.copy(surgeries = listOf(PROVIDED_SURGERY_WITH_NAME))
        )
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                message = "Could not find surgery config for input 'surgery one'",
                patientId = HASHED_ID_IN_BASE64,
                category = CurationCategory.SURGERY,
                feedInput = PROVIDED_SURGERY_NAME
            )
        )
    }

    @Test
    fun `Should filter surgery entry when surgery is set to be ignore in curation`() {
        val ignoredName = "Geen ingreep- operatie uitgesteld"

        every { surgeryCuration.find(PROVIDED_SURGERY_NAME) } returns setOf(
            SurgeryConfig(input = PROVIDED_SURGERY_NAME, ignore = false, name = CURATED_SURGERY_NAME, treatmentType = OtherTreatmentType.CYTOREDUCTIVE_SURGERY)
        )
        every { surgeryCuration.find(ignoredName) } returns setOf(
            SurgeryConfig(input = ignoredName, ignore = true, name = "<ignore>", treatmentType = OtherTreatmentType.OTHER_SURGERY)
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
                status = SurgeryStatus.FINISHED,
                treatmentType = OtherTreatmentType.CYTOREDUCTIVE_SURGERY,
            )
        )
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should return surgery entry when surgery name is null without curation`() {
        val surgery = FeedTestData.createFeedSurgery(null)
        val result = extractor.extract(FEED_PATIENT_RECORD.copy(surgeries = listOf(surgery)))

        assertThat(result.extracted).containsExactly(
            Surgery(name = surgery.name, endDate = surgery.endDate, status = SurgeryStatus.FINISHED, treatmentType = OtherTreatmentType.OTHER_SURGERY),
        )
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should return an unknown surgery by default if there is no surgery name`() {
        val surgery = FeedTestData.createFeedSurgery(null)
        val result = extractor.extract(FEED_PATIENT_RECORD.copy(surgeries = listOf(surgery)))

        assertThat(result.extracted).containsExactly(
            Surgery(name = surgery.name, endDate = surgery.endDate, status = SurgeryStatus.FINISHED, treatmentType = OtherTreatmentType.OTHER_SURGERY),
        )
        assertThat(result.evaluation.warnings).isEmpty()
    }
}