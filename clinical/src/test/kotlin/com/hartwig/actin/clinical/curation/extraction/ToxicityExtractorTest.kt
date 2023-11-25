package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import com.hartwig.actin.clinical.feed.digitalfile.DigitalFileEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "cannot curate"

class ToxicityExtractorTest {
    private val extractor = ToxicityExtractor(TestCurationFactory.createProperTestCurationDatabase())

    @Test
    fun `Should curate questionnaire toxicities`() {
        val date = LocalDate.of(2018, 5, 21)
        val inputs = listOf("neuropathy gr3", CANNOT_CURATE)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(date = date, unresolvedToxicities = inputs)
        val (toxicities, evaluation) = extractor.extract(PATIENT_ID, emptyList(), questionnaire)
        assertThat(toxicities).hasSize(1)
        val toxicity = toxicities[0]
        assertThat(toxicity.name()).isEqualTo("neuropathy")
        assertThat(toxicity.categories()).containsExactly("neuro")
        assertThat(toxicity.evaluatedDate()).isEqualTo(date)
        assertThat(toxicity.source()).isEqualTo(ToxicitySource.QUESTIONNAIRE)
        assertThat(toxicity.grade()).isEqualTo(Integer.valueOf(3))

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID, CurationCategory.TOXICITY, CANNOT_CURATE, "Could not find toxicity config for input '$CANNOT_CURATE'"
            )
        )
        assertThat(evaluation.toxicityEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())

        assertThat(extractor.extract(PATIENT_ID, emptyList(), questionnaire.copy(unresolvedToxicities = null)).extracted).isEmpty()
    }

    @Test
    fun `Should translate toxicities`() {
        val names = listOf("Pijn", CANNOT_CURATE)
        val inputs = names.map { name ->
            DigitalFileEntry(
                subject = PATIENT_ID,
                authored = LocalDate.of(2020, 11, 11),
                itemText = name,
                itemAnswerValueValueString = "3",
                description = ""
            )
        }
        val (toxicities, evaluation) = extractor.extract(PATIENT_ID, inputs, null)
        assertThat(toxicities).hasSize(2).anyMatch { it.name() == "Pain" }

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.TOXICITY_TRANSLATION,
                CANNOT_CURATE,
                "No translation found for toxicity: '$CANNOT_CURATE'"
            )
        )
        assertThat(evaluation.toxicityTranslationEvaluatedInputs).isEqualTo(names.map(String::lowercase).toSet())
    }
}