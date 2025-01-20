package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.feed.standard.EhrTestData.createEhrPatientRecord
import com.hartwig.actin.clinical.feed.standard.ProvidedOtherCondition
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.OtherCondition
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val OTHER_CONDITION_NAME = "prior_condition"

private val OTHER_CONDITION = OtherCondition(
    name = OTHER_CONDITION_NAME,
    icdCodes = setOf(IcdCode("icdCode"))
)

private val EHR_OTHER_CONDITION = ProvidedOtherCondition(
    name = OTHER_CONDITION_NAME,
    startDate = LocalDate.of(2024, 2, 27),
    endDate = LocalDate.of(2024, 2, 28)
)

private val EHR_PATIENT_RECORD_WITH_OTHER_CONDITIONS = createEhrPatientRecord().copy(
    priorOtherConditions = listOf(
        EHR_OTHER_CONDITION
    )
)

class StandardOtherConditionsExtractorTest {

    private val otherConditionsCuration = mockk<CurationDatabase<NonOncologicalHistoryConfig>>()
    private val extractor = StandardOtherConditionsExtractor(otherConditionsCuration)

    @Test
    fun `Should extract other conditions and curate the condition`() {
        val anotherCondition = "another_condition"
        every { otherConditionsCuration.find(OTHER_CONDITION_NAME) } returns setOf(
            NonOncologicalHistoryConfig(
                input = OTHER_CONDITION_NAME,
                ignore = false,
                otherCondition = OTHER_CONDITION
            ),
            NonOncologicalHistoryConfig(
                input = OTHER_CONDITION_NAME,
                ignore = false,
                otherCondition = OTHER_CONDITION.copy(anotherCondition)
            )
        )
        val result = extractor.extract(EHR_PATIENT_RECORD_WITH_OTHER_CONDITIONS)
        assertThat(result.extracted).containsExactly(
            OTHER_CONDITION.copy(year = 2024, month = 2),
            OTHER_CONDITION.copy(year = 2024, month = 2, name = anotherCondition)
        )
    }

    @Test
    fun `Should return curation warnings when no curation found`() {
        every { otherConditionsCuration.find(OTHER_CONDITION_NAME) } returns emptySet()
        val result = extractor.extract(EHR_PATIENT_RECORD_WITH_OTHER_CONDITIONS)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                EHR_PATIENT_RECORD_WITH_OTHER_CONDITIONS.patientDetails.hashedId,
                CurationCategory.NON_ONCOLOGICAL_HISTORY,
                OTHER_CONDITION_NAME,
                "Could not find non-oncological history config for input 'prior_condition'"
            )
        )
    }
}