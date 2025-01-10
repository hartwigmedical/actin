package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.feed.standard.EhrTestData.createEhrPatientRecord
import com.hartwig.actin.clinical.feed.standard.ProvidedPriorOtherCondition
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PRIOR_CONDITION_NAME = "prior_condition"

private val PRIOR_OTHER_CONDITION = PriorOtherCondition(
    name = PRIOR_CONDITION_NAME,
    icdCodes = setOf(IcdCode("icdCode")),
    isContraindicationForTherapy = true
)

private val EHR_PRIOR_OTHER_CONDITION = ProvidedPriorOtherCondition(
    name = PRIOR_CONDITION_NAME,
    startDate = LocalDate.of(2024, 2, 27),
    endDate = LocalDate.of(2024, 2, 28)
)

private val EHR_PATIENT_RECORD_WITH_PRIOR_CONDITIONS = createEhrPatientRecord().copy(
    priorOtherConditions = listOf(
        EHR_PRIOR_OTHER_CONDITION
    )
)

class StandardPriorOtherConditionsExtractorTest {

    private val priorOtherConditionsCuration = mockk<CurationDatabase<NonOncologicalHistoryConfig>>()
    private val extractor = StandardPriorOtherConditionsExtractor(priorOtherConditionsCuration)

    @Test
    fun `Should extract prior other conditions and curate the condition`() {
        val anotherPriorCondition = "another_prior_condition"
        every { priorOtherConditionsCuration.find(PRIOR_CONDITION_NAME) } returns setOf(
            NonOncologicalHistoryConfig(
                input = PRIOR_CONDITION_NAME,
                ignore = false,
                priorOtherCondition = PRIOR_OTHER_CONDITION
            ),
            NonOncologicalHistoryConfig(
                input = PRIOR_CONDITION_NAME,
                ignore = false,
                priorOtherCondition = PRIOR_OTHER_CONDITION.copy(anotherPriorCondition)
            )
        )
        val result = extractor.extract(EHR_PATIENT_RECORD_WITH_PRIOR_CONDITIONS)
        assertThat(result.extracted).containsExactly(
            PRIOR_OTHER_CONDITION.copy(year = 2024, month = 2),
            PRIOR_OTHER_CONDITION.copy(year = 2024, month = 2, name = anotherPriorCondition)
        )
    }

    @Test
    fun `Should return curation warnings when no curation found`() {
        every { priorOtherConditionsCuration.find(PRIOR_CONDITION_NAME) } returns emptySet()
        val result = extractor.extract(EHR_PATIENT_RECORD_WITH_PRIOR_CONDITIONS)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                EHR_PATIENT_RECORD_WITH_PRIOR_CONDITIONS.patientDetails.hashedId,
                CurationCategory.NON_ONCOLOGICAL_HISTORY,
                PRIOR_CONDITION_NAME,
                "Could not find non-oncological history config for input 'prior_condition'"
            )
        )
    }
}