package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "cannot curate"

class PriorOtherConditionsExtractorTest {
    private val extractor = PriorOtherConditionsExtractor(TestCurationFactory.createProperTestCurationDatabase())

    @Test
    fun `Should curate prior other conditions`() {
        val inputs = listOf("sickness", "not a condition", CANNOT_CURATE)
        val questionnaire = TestCurationFactory.emptyQuestionnaire()
            .copy(nonOncologicalHistory = inputs)
        val (priorOtherConditions, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(priorOtherConditions).hasSize(1)
        assertThat(priorOtherConditions[0].name()).isEqualTo("sick")

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