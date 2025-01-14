package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.OtherCondition
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PATIENT_ID = "patient1"

private const val NON_ONCOLOGICAL_INPUT = "Non-oncological input"

private const val CANNOT_CURATE = "cannot curate"

private const val OTHER_CONDITION_INTERPRETATION = "Prior condition interpretation"

class OtherConditionsExtractorTest {
    private val extractor = OtherConditionsExtractor(
        TestCurationFactory.curationDatabase(
            NonOncologicalHistoryConfig(
                input = NON_ONCOLOGICAL_INPUT,
                ignore = false,
                lvef = null,
                otherCondition = OtherCondition(
                    name = OTHER_CONDITION_INTERPRETATION,
                    icdCodes = setOf(IcdCode("icd"))
                )
            )
        )
    )

    @Test
    fun `Should curate other conditions`() {
        val inputs = listOf(NON_ONCOLOGICAL_INPUT, CANNOT_CURATE)
        val questionnaire = TestCurationFactory.emptyQuestionnaire()
            .copy(nonOncologicalHistory = inputs)
        val (otherConditions, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(otherConditions).hasSize(1)
        assertThat(otherConditions[0].name).isEqualTo(OTHER_CONDITION_INTERPRETATION)

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.NON_ONCOLOGICAL_HISTORY,
                CANNOT_CURATE,
                "Could not find non-oncological history config for input '$CANNOT_CURATE'"
            )
        )
        assertThat(evaluation.nonOncologicalHistoryEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())
    }

    @Test
    fun `Should extract empty list when questionnaire non oncological history list is null`() {
        assertThat(extractor.extract(PATIENT_ID, null).extracted).isEmpty()
    }
}