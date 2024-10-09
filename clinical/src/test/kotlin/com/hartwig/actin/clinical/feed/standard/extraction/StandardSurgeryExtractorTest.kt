package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.config.SurgeryNameConfig
import com.hartwig.actin.clinical.feed.standard.EhrTestData
import com.hartwig.actin.clinical.feed.standard.EhrTestData.createEhrPatientRecord
import com.hartwig.actin.clinical.feed.standard.HASHED_ID_IN_BASE64
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


private const val PROVIDED_SURGERY_NAME = "surgery one"
private const val CURATED_SURGERY_NAME = "surgery 1"
private const val PROVIDED_SURGERY_NAME_TO_BE_IGNORED = "Geen ingreep- operatie uitgesteld"
private val PROVIDED_EHR_PATIENT_RECORD = createEhrPatientRecord()
private val PROVIDED_SURGERY_WITH_NAME = EhrTestData.createEhrSurgery(PROVIDED_SURGERY_NAME)
private val PROVIDED_SURGERY_TO_BE_IGNORED = EhrTestData.createEhrSurgery(PROVIDED_SURGERY_NAME_TO_BE_IGNORED)
private val PROVIDED_SURGERY_WITHOUT_NAME = EhrTestData.createEhrSurgery()

private val CURATED_SURGERY_CONFIG = SurgeryNameConfig(
    input = PROVIDED_SURGERY_NAME,
    ignore = false,
    name = CURATED_SURGERY_NAME
)
private val CURATED_SURGERY_CONFIG_IGNORE = SurgeryNameConfig(
    input = PROVIDED_SURGERY_NAME_TO_BE_IGNORED,
    ignore = true,
    name = "<ignore>"
)

class StandardSurgeryExtractorTest {

    private val surgeryNameCuration = mockk<CurationDatabase<SurgeryNameConfig>>()
    private val extractor = StandardSurgeryExtractor(surgeryNameCuration)

    @Test
    fun `Should filter surgery entry and warn when no curation for surgery name`() {

        every { surgeryNameCuration.find(PROVIDED_SURGERY_NAME) } returns emptySet()
        val result = extractor.extract(
            PROVIDED_EHR_PATIENT_RECORD.copy(
                surgeries = listOf(PROVIDED_SURGERY_WITH_NAME)
            )
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
        every { surgeryNameCuration.find(PROVIDED_SURGERY_NAME) } returns setOf(CURATED_SURGERY_CONFIG)
        every { surgeryNameCuration.find(PROVIDED_SURGERY_NAME_TO_BE_IGNORED) } returns setOf(CURATED_SURGERY_CONFIG_IGNORE)

        val result = extractor.extract(
            PROVIDED_EHR_PATIENT_RECORD.copy(
                surgeries = listOf(PROVIDED_SURGERY_WITH_NAME, PROVIDED_SURGERY_TO_BE_IGNORED)
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

        val result = extractor.extract(
            PROVIDED_EHR_PATIENT_RECORD.copy(
                surgeries = listOf(PROVIDED_SURGERY_WITHOUT_NAME)
            )
        )
        assertThat(result.extracted).isNotEmpty()
        assertThat(result.extracted.size).isEqualTo(1)
        assertThat(result.extracted).containsExactly(
            Surgery(
                name = PROVIDED_SURGERY_WITHOUT_NAME.surgeryName,
                endDate = PROVIDED_SURGERY_WITHOUT_NAME.endDate,
                status = SurgeryStatus.FINISHED
            )
        )
        assertThat(result.evaluation.warnings).isEmpty()
    }
}