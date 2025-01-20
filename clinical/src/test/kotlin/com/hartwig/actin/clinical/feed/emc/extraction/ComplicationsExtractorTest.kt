package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.ComplicationConfig
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.IcdCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "cannot curate"

class ComplicationsExtractorTest {
    private val curationDatabase = TestCurationFactory.curationDatabase(
        ComplicationConfig(
            input = "term",
            ignore = false,
            curated = Complication(name = "Curated", year = null, month = null, icdCodes = setOf(IcdCode("code"))),
            impliesUnknownComplicationState = false
        ),
        ComplicationConfig(
            input = "none",
            ignore = true,
            curated = null,
            impliesUnknownComplicationState = false
        ),
        ComplicationConfig(
            input = "unknown",
            ignore = false,
            curated = null,
            impliesUnknownComplicationState = true
        )
    )

    private val extractor = ComplicationsExtractor(curationDatabase)

    @Test
    fun `Should extract null for null or empty input`() {
        assertNullExtractionWithoutWarnings(null)
        assertNullExtractionWithoutWarnings(emptyList())
    }

    private fun assertNullExtractionWithoutWarnings(input: List<String>?) {
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(complications = input)
        val (complications, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(complications).isNull()
        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.complicationEvaluatedInputs).isEmpty()
    }

    @Test
    fun `Should curate complications`() {
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
        assertThat(evaluation.complicationEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())
    }

    @Test
    fun `Should extract empty list for ignore input`() {
        val inputs = listOf("none")
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(complications = inputs)
        val (ignore, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(ignore).isEmpty()
        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.complicationEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())
    }

    @Test
    fun `Should extract null for complication with unknown state`() {
        val inputs = listOf("Unknown")
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(complications = inputs)
        val (unknown, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(unknown).isNull()
        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.complicationEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())
    }
}