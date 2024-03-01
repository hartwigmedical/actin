package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.clinical.feed.standard.EhrTestData.createEhrPatientRecord
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val PRIOR_CONDITION_NAME = "prior_condition"

private val PRIOR_OTHER_CONDITION = PriorOtherCondition(
    name = PRIOR_CONDITION_NAME,
    category = "category",
    isContraindicationForTherapy = true
)

private val EHR_PRIOR_OTHER_CONDITION = EhrPriorOtherCondition(
    name = PRIOR_CONDITION_NAME,
    startDate = LocalDate.of(2024, 2, 27),
    endDate = LocalDate.of(2024, 2, 28)
)

private val EHR_PATIENT_RECORD_WITH_PRIOR_CONDITIONS = createEhrPatientRecord().copy(
    priorOtherConditions = listOf(
        EHR_PRIOR_OTHER_CONDITION
    )
)

class EhrPriorOtherConditionsExtractorTest {

    private val priorOtherConditionsCuration = mockk<CurationDatabase<NonOncologicalHistoryConfig>>()
    private val oncologicalHistoryCuration = mockk<CurationDatabase<TreatmentHistoryEntryConfig>>()
    private val extractor = EhrPriorOtherConditionsExtractor(priorOtherConditionsCuration, oncologicalHistoryCuration)


    @Test
    fun `Should extract prior other conditions and curate the condition`() {
        every { priorOtherConditionsCuration.find(PRIOR_CONDITION_NAME) } returns setOf(
            NonOncologicalHistoryConfig(
                input = PRIOR_CONDITION_NAME,
                ignore = false,
                priorOtherCondition = PRIOR_OTHER_CONDITION
            )
        )
        every { oncologicalHistoryCuration.find(PRIOR_CONDITION_NAME) } returns emptySet()
        val result = extractor.extract(EHR_PATIENT_RECORD_WITH_PRIOR_CONDITIONS)
        assertThat(result.extracted).containsExactly(PRIOR_OTHER_CONDITION.copy(year = 2024, month = 2))
    }

    @Test
    fun `Should skip prior conditions when condition name is in oncological curation`() {
        every { priorOtherConditionsCuration.find(PRIOR_CONDITION_NAME) } returns emptySet()
        every { oncologicalHistoryCuration.find(PRIOR_CONDITION_NAME) } returns setOf(
            TreatmentHistoryEntryConfig(PRIOR_CONDITION_NAME, false)
        )
        val result = extractor.extract(EHR_PATIENT_RECORD_WITH_PRIOR_CONDITIONS)
        assertThat(result.extracted).isEmpty()
    }

    @Test
    fun `Should return curation warnings when no curation found`() {
        every { priorOtherConditionsCuration.find(PRIOR_CONDITION_NAME) } returns emptySet()
        every { oncologicalHistoryCuration.find(PRIOR_CONDITION_NAME) } returns emptySet()
        val result = extractor.extract(EHR_PATIENT_RECORD_WITH_PRIOR_CONDITIONS)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                EHR_PATIENT_RECORD_WITH_PRIOR_CONDITIONS.patientDetails.hashedIdBase64(),
                CurationCategory.NON_ONCOLOGICAL_HISTORY,
                PRIOR_CONDITION_NAME,
                "Could not find non-oncological history config for input 'prior_condition'"
            )
        )
    }

}