package com.hartwig.actin.clinical.curation.translation

import com.hartwig.actin.datamodel.clinical.ingestion.UnusedCurationConfig
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TranslationDatabaseTest {

    @Test
    fun `Should return null translation when input key is not found`() {
        val translationDatabase = TranslationDatabase<String>(mapOf(), CurationCategory.TOXICITY_TRANSLATION) { emptySet() }
        assertThat(translationDatabase.find("input")).isNull()
    }

    @Test
    fun `Should return translation translations when input key is found`() {
        val translation = Translation("input", "translated")
        val translationDatabase = TranslationDatabase(
            mapOf("input" to translation), CurationCategory.TOXICITY_TRANSLATION
        ) { emptySet() }
        assertThat(translationDatabase.find("input")).isEqualTo(translation)
    }

    @Test
    fun `Should report unused translations`() {
        val firstTranslation = Translation("input1", "translated1")
        val secondTranslation = Translation("input2", "translated2")
        val translationDatabase = TranslationDatabase(
            mapOf(firstTranslation.input to firstTranslation, secondTranslation.input to secondTranslation),
            CurationCategory.TOXICITY_TRANSLATION
        ) { it.toxicityTranslationEvaluatedInputs }
        assertThat(
            translationDatabase.reportUnusedTranslations(
                listOf(
                    CurationExtractionEvaluation(
                        toxicityTranslationEvaluatedInputs = setOf(
                            firstTranslation
                        )
                    )
                )
            )
        ).containsExactly(UnusedCurationConfig(CurationCategory.TOXICITY_TRANSLATION.categoryName, secondTranslation.input))
    }
}