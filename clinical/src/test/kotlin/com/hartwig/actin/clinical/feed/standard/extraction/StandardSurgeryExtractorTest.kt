package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.config.SurgeryConfig
import com.hartwig.actin.clinical.feed.standard.EhrTestData
import com.hartwig.actin.clinical.feed.standard.EhrTestData.createEhrPatientRecord
import com.hartwig.actin.clinical.feed.standard.HASHED_ID_IN_BASE64
import com.hartwig.actin.clinical.feed.standard.ProvidedPriorOtherCondition
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test


private const val PROVIDED_SURGERY_NAME = "surgery one"
private const val CURATED_SURGERY_NAME = "surgery 1"
private const val PROVIDED_SURGERY_NAME_TO_BE_IGNORED = "Geen ingreep- operatie uitgesteld"
private val PROVIDED_EHR_PATIENT_RECORD = createEhrPatientRecord()

private val PROVIDED_SURGERY_WITH_NAME = EhrTestData.createEhrSurgery(PROVIDED_SURGERY_NAME)
private val PROVIDED_SURGERY_TO_BE_IGNORED = EhrTestData.createEhrSurgery(PROVIDED_SURGERY_NAME_TO_BE_IGNORED)
private val PROVIDED_SURGERY_WITHOUT_NAME = EhrTestData.createEhrSurgery(null)

private val CURATED_SURGERY_CONFIG = SurgeryConfig(
    input = PROVIDED_SURGERY_NAME,
    ignore = false,
    name = CURATED_SURGERY_NAME,
    endDate = LocalDate.of(2024, 10, 18),
    status = SurgeryStatus.FINISHED
)
private val CURATED_SURGERY_CONFIG_IGNORE = SurgeryConfig(
    input = PROVIDED_SURGERY_NAME_TO_BE_IGNORED,
    ignore = true,
    name = "<ignore>"
)

class StandardSurgeryExtractorTest {

    private val surgeryNameCuration = mockk<CurationDatabase<SurgeryConfig>>()
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
                category = CurationCategory.SURGERY,
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

    @Test
    fun `Should curate prior other condition as surgery when name matches curation, and return no warnings if no match`() {
        val input = "surgery in prior condition"
        every { surgeryNameCuration.find(input) } returns setOf(CURATED_SURGERY_CONFIG)
        val anotherInput = "another condition"
        every { surgeryNameCuration.find(anotherInput) } returns emptySet()
        val priorConditionEndDate = LocalDate.of(2024, 10, 24)
        val result = extractor.extract(
            PROVIDED_EHR_PATIENT_RECORD.copy(
                priorOtherConditions = listOf(
                    ProvidedPriorOtherCondition(input, endDate = priorConditionEndDate),
                    ProvidedPriorOtherCondition(anotherInput)
                )
            )
        )
        assertThat(result.extracted).isNotEmpty()
        assertThat(result.extracted.size).isEqualTo(1)
        assertThat(result.extracted).containsExactly(
            Surgery(
                name = CURATED_SURGERY_CONFIG.name,
                endDate = priorConditionEndDate,
                status = CURATED_SURGERY_CONFIG.status!!
            )
        )
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should throw when surgery is curated and surgery has no end date and neither does the curation `() {
        val input = "surgery in prior condition"
        every { surgeryNameCuration.find(input) } returns setOf(CURATED_SURGERY_CONFIG.copy(endDate = null))
        assertThatThrownBy {
            extractor.extract(
                PROVIDED_EHR_PATIENT_RECORD.copy(
                    priorOtherConditions = listOf(ProvidedPriorOtherCondition(input))
                )
            )
        }.isInstanceOf(IllegalStateException::class.java)
    }
}