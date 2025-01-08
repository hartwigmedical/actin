package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PATIENT_ID = "patient1"

private const val NON_ONCOLOGICAL_INPUT = "Non-oncological input"

private const val CANNOT_CURATE = "cannot curate"

private const val PRIOR_CONDITION_INTERPRETATION = "Prior condition interpretation"

class PriorOtherConditionsExtractorTest {
    private val extractor = PriorOtherConditionsExtractor(
        TestCurationFactory.curationDatabase(
            NonOncologicalHistoryConfig(
                input = NON_ONCOLOGICAL_INPUT,
                ignore = false,
                lvef = null,
                priorOtherCondition = PriorOtherCondition(
                    name = PRIOR_CONDITION_INTERPRETATION,
                    icdCodes = setOf(IcdCode("icd")),
                    isContraindicationForTherapy = false
                )
            )
        )
    )

    @Test
    fun `Should curate prior other conditions`() {
        val inputs = listOf(NON_ONCOLOGICAL_INPUT, CANNOT_CURATE)
        val questionnaire = TestCurationFactory.emptyQuestionnaire()
            .copy(nonOncologicalHistory = inputs)
        val (priorOtherConditions, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(priorOtherConditions).hasSize(1)
        assertThat(priorOtherConditions[0].name).isEqualTo(PRIOR_CONDITION_INTERPRETATION)

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