package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.ToxicityConfig
import com.hartwig.actin.clinical.curation.translation.Translation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.feed.emc.digitalfile.DigitalFileEntry
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "cannot curate"


private const val TOXICITY_INPUT = "Toxicity input"

private const val TOXICITY_NAME = "Toxicity name"

private const val TOXICITY_CATEGORY = "Toxicity category"

private const val TOXICITY_ICD_CODE = "Toxicity icd code"

private const val TOXICITY_EXTENSION_CODE = "Toxicity extension"

private const val TOXICITY_TRANSLATED = "Toxicity translated"

class ToxicityExtractorTest {
    private val extractor = ToxicityExtractor(
        TestCurationFactory.curationDatabase(
            ToxicityConfig(
                input = TOXICITY_INPUT,
                ignore = false,
                name = TOXICITY_NAME,
                categories = setOf(TOXICITY_CATEGORY),
                grade = 3,
                icdCode = IcdCode(TOXICITY_ICD_CODE, TOXICITY_EXTENSION_CODE)
            )
        ),
        TranslationDatabase(
            mapOf(TOXICITY_INPUT to Translation(TOXICITY_INPUT, TOXICITY_TRANSLATED)),
            CurationCategory.TOXICITY_TRANSLATION
        ) { emptySet() }
    )

    @Test
    fun `Should curate and translate questionnaire toxicities`() {
        val date = LocalDate.of(2018, 5, 21)
        val inputs = listOf(TOXICITY_INPUT, CANNOT_CURATE)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(date = date, unresolvedToxicities = inputs)
        val (toxicities, evaluation) = extractor.extract(PATIENT_ID, emptyList(), questionnaire)
        assertThat(toxicities).hasSize(1)
        val toxicity = toxicities[0]
        assertThat(toxicity.name).isEqualTo(TOXICITY_NAME)
        assertThat(toxicity.categories).containsExactly(TOXICITY_CATEGORY)
        assertThat(toxicity.icdCode.mainCode).isEqualTo(TOXICITY_ICD_CODE)
        assertThat(toxicity.icdCode.extensionCode).isEqualTo(TOXICITY_EXTENSION_CODE)
        assertThat(toxicity.evaluatedDate).isEqualTo(date)
        assertThat(toxicity.source).isEqualTo(ToxicitySource.QUESTIONNAIRE)
        assertThat(toxicity.grade).isEqualTo(Integer.valueOf(3))

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID, CurationCategory.TOXICITY, CANNOT_CURATE, "Could not find toxicity config for input '$CANNOT_CURATE'"
            )
        )
        assertThat(evaluation.toxicityEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())

        assertThat(extractor.extract(PATIENT_ID, emptyList(), questionnaire.copy(unresolvedToxicities = null)).extracted).isEmpty()
    }

    @Test
    fun `Should translate feed toxicities`() {
        val inputs = setOf(TOXICITY_INPUT, CANNOT_CURATE).map { name ->
            DigitalFileEntry(
                subject = PATIENT_ID,
                authored = LocalDate.of(2020, 11, 11),
                itemText = name,
                itemAnswerValueValueString = "3",
                description = ""
            )
        }
        val (toxicities, evaluation) = extractor.extract(PATIENT_ID, inputs, null)
        assertThat(toxicities).hasSize(2).anyMatch { it.name == TOXICITY_TRANSLATED }

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.TOXICITY_TRANSLATION,
                CANNOT_CURATE,
                "No translation found for toxicity: '$CANNOT_CURATE'"
            )
        )
        assertThat(evaluation.toxicityTranslationEvaluatedInputs).isEqualTo(setOf(Translation(TOXICITY_INPUT, TOXICITY_TRANSLATED)))
    }
}