package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.OtherCondition
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "cannot curate"
private const val OTHER_CONDITION_INPUT = "Non-oncological input"
private const val OTHER_CONDITION_INTERPRETATION = "Prior condition interpretation"

class ComorbidityExtractorTest {
    private val complicationCurationDatabase = TestCurationFactory.curationDatabase(
        ComorbidityConfig(
            input = "term",
            ignore = false,
            curated = Complication(name = "Curated", year = null, month = null, icdCodes = setOf(IcdCode("code")))
        ),
        ComorbidityConfig(
            input = "none",
            ignore = true,
            curated = null
        )
    )

    @Test
    fun `Should extract empty list for null or empty input`() {
        assertEmptyExtractionWithoutWarnings(null)
        assertEmptyExtractionWithoutWarnings(emptyList())
    }

    private fun assertEmptyExtractionWithoutWarnings(input: List<String>?) {
        val extractor = ComorbidityExtractor(complicationCurationDatabase)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(complications = input)
        val (complications, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(complications).isEmpty()
        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.comorbidityEvaluatedInputs).isEmpty()
    }

    @Test
    fun `Should curate complications`() {
        val extractor = ComorbidityExtractor(complicationCurationDatabase)
        val inputs = listOf("term", CANNOT_CURATE)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(complications = inputs)
        val (complications, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(complications).isNotNull
        assertThat(complications!!).hasSize(1)
        assertThat(complications).anyMatch { it.name == "Curated" }

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID, CurationCategory.COMPLICATION, CANNOT_CURATE, "Could not find complication config for input '$CANNOT_CURATE'"
            )
        )
        assertThat(evaluation.comorbidityEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())
    }

    @Test
    fun `Should extract empty list for ignore input`() {
        val extractor = ComorbidityExtractor(complicationCurationDatabase)
        val inputs = listOf("none")
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(complications = inputs)
        val (ignore, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(ignore).isEmpty()
        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.comorbidityEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())
    }

    @Test
    fun `Should curate other conditions`() {
        val extractor = ComorbidityExtractor(
            TestCurationFactory.curationDatabase(
                ComorbidityConfig(
                    input = OTHER_CONDITION_INPUT,
                    ignore = false,
                    lvef = null,
                    curated = OtherCondition(
                        name = OTHER_CONDITION_INTERPRETATION,
                        icdCodes = setOf(IcdCode("icd"))
                    )
                )
            )
        )
        val inputs = listOf(OTHER_CONDITION_INPUT, CANNOT_CURATE)
        val questionnaire = TestCurationFactory.emptyQuestionnaire()
            .copy(nonOncologicalHistory = inputs)
        val (otherConditions, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(otherConditions).isNotNull
        assertThat(otherConditions).hasSize(1)
        assertThat(otherConditions!![0].name).isEqualTo(OTHER_CONDITION_INTERPRETATION)

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.NON_ONCOLOGICAL_HISTORY,
                CANNOT_CURATE,
                "Could not find non-oncological history config for input '$CANNOT_CURATE'"
            )
        )
        assertThat(evaluation.comorbidityEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())
    }
}