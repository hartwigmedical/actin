package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.clinical.curation.config.ToxicityCuration
import com.hartwig.actin.clinical.curation.translation.Translation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.feed.emc.digitalfile.DigitalFileEntry
import com.hartwig.actin.clinical.feed.emc.intolerance.IntoleranceEntry
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.OtherCondition
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "cannot curate"
private const val OTHER_CONDITION_INPUT = "Non-oncological input"
private const val OTHER_CONDITION_INTERPRETATION = "Prior condition interpretation"
private const val TOXICITY_INPUT = "Toxicity input"
private const val OTHER_TOXICITY_INPUT = "Other toxicity input"
private const val TOXICITY_NAME = "Toxicity name"
private const val TOXICITY_ICD_CODE = "Toxicity icd code"
private const val TOXICITY_EXTENSION_CODE = "Toxicity extension"
private const val TOXICITY_TRANSLATED = "Toxicity translated"

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

    private val toxicityCurationDatabase = TestCurationFactory.curationDatabase(
        ComorbidityConfig(
            input = TOXICITY_INPUT,
            ignore = false,
            curated = ToxicityCuration(
                name = TOXICITY_NAME,
                grade = 3,
                icdCodes = setOf(IcdCode(TOXICITY_ICD_CODE, TOXICITY_EXTENSION_CODE))
            )
        )
    )

    private val toxicityTranslationDatabase = TranslationDatabase(
            mapOf(
                TOXICITY_INPUT to Translation(TOXICITY_INPUT, TOXICITY_TRANSLATED),
                OTHER_TOXICITY_INPUT to Translation(OTHER_TOXICITY_INPUT, TOXICITY_TRANSLATED)
            ),
            CurationCategory.TOXICITY_TRANSLATION
        ) { emptySet() }

    @Test
    fun `Should extract empty list for null or empty input`() {
        assertEmptyExtractionWithoutWarnings(null)
        assertEmptyExtractionWithoutWarnings(emptyList())
    }

    private fun assertEmptyExtractionWithoutWarnings(input: List<String>?) {
        val extractor = ComorbidityExtractor(complicationCurationDatabase, toxicityTranslationDatabase)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(complications = input)
        val (complications, evaluation) = extractor.extract(PATIENT_ID, questionnaire, emptyList(), emptyList())
        assertThat(complications).isEmpty()
        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.comorbidityEvaluatedInputs).isEmpty()
    }

    @Test
    fun `Should curate complications`() {
        val extractor = ComorbidityExtractor(complicationCurationDatabase, toxicityTranslationDatabase)
        val inputs = listOf("term", CANNOT_CURATE)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(complications = inputs)
        val (complications, evaluation) = extractor.extract(PATIENT_ID, questionnaire, emptyList(), emptyList())
        assertThat(complications).isNotNull
        assertThat(complications).hasSize(1)
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
        val extractor = ComorbidityExtractor(complicationCurationDatabase, toxicityTranslationDatabase)
        val inputs = listOf("none")
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(complications = inputs)
        val (ignore, evaluation) = extractor.extract(PATIENT_ID, questionnaire, emptyList(), emptyList())
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
            ),
            toxicityTranslationDatabase
        )
        val inputs = listOf(OTHER_CONDITION_INPUT, CANNOT_CURATE)
        val questionnaire = TestCurationFactory.emptyQuestionnaire()
            .copy(nonOncologicalHistory = inputs)
        val (otherConditions, evaluation) = extractor.extract(PATIENT_ID, questionnaire, emptyList(), emptyList())
        assertThat(otherConditions).isNotNull
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
        assertThat(evaluation.comorbidityEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())
    }

    @Test
    fun `Should extract curated intolerances`() {
        val intoleranceInput = "Intolerance input"
        val curatedIntolerance = "Curated intolerance"
        val medicationIntoleranceInput = "Paracetamol"
        val curatedMedicationIntolerance = "Paracetamol"
        val icd = "ICD"
        val cannotCurate = "Cannot curate"

        val entry = IntoleranceEntry(
            subject = PATIENT_ID,
            assertedDate = LocalDate.now(),
            category = "",
            categoryAllergyCategoryDisplay = "",
            codeText = "",
            isSideEffect = "",
            clinicalStatus = "",
            verificationStatus = "",
            criticality = ""
        )

        val extractor = ComorbidityExtractor(
            TestCurationFactory.curationDatabase(
                ComorbidityConfig(
                    input = intoleranceInput,
                    ignore = false,
                    curated = Intolerance(name = curatedIntolerance, icdCodes = setOf(IcdCode(icd, null)))
                ),
                ComorbidityConfig(
                    input = medicationIntoleranceInput,
                    ignore = false,
                    curated = Intolerance(name = curatedMedicationIntolerance, icdCodes = setOf(IcdCode(icd, null)))
                )
            ),
            toxicityTranslationDatabase
        )
        val inputs = listOf(intoleranceInput, cannotCurate)
        val intoleranceEntries = inputs.map { entry.copy(codeText = it) }
        val (curated, evaluation) = extractor.extract(PATIENT_ID, TestCurationFactory.emptyQuestionnaire(), emptyList(), intoleranceEntries)
        assertThat(curated).hasSize(2)
        assertThat(curated[0].name).isEqualTo(curatedIntolerance)
        assertThat(curated[0].icdCodes.first().mainCode).isEqualTo(icd)
        assertThat(curated[0].icdCodes.first().extensionCode).isNull()

        assertThat(curated[1].name).isEqualTo(cannotCurate)

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID, CurationCategory.INTOLERANCE, cannotCurate, "Could not find intolerance config for input 'Cannot curate'"
            )
        )
        assertThat(evaluation.comorbidityEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())
    }

    @Test
    fun `Should curate and translate questionnaire toxicities`() {
        val date = LocalDate.of(2018, 5, 21)
        val inputs = listOf(TOXICITY_INPUT, CANNOT_CURATE)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(date = date, unresolvedToxicities = inputs)
        val extractor = ComorbidityExtractor(toxicityCurationDatabase, toxicityTranslationDatabase)
        val (toxicities, evaluation) = extractor.extract(PATIENT_ID, questionnaire, emptyList(), emptyList())
        assertThat(toxicities).hasSize(1)
        val toxicity = toxicities[0] as Toxicity
        assertThat(toxicity.name).isEqualTo(TOXICITY_NAME)
        assertThat(toxicity.icdCodes.first().mainCode).isEqualTo(TOXICITY_ICD_CODE)
        assertThat(toxicity.icdCodes.first().extensionCode).isEqualTo(TOXICITY_EXTENSION_CODE)
        assertThat(toxicity.evaluatedDate).isEqualTo(date)
        assertThat(toxicity.source).isEqualTo(ToxicitySource.QUESTIONNAIRE)
        assertThat(toxicity.grade).isEqualTo(Integer.valueOf(3))

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID, CurationCategory.TOXICITY, CANNOT_CURATE, "Could not find toxicity config for input '$CANNOT_CURATE'"
            )
        )
        assertThat(evaluation.comorbidityEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())
    }

    @Test
    fun `Should return empty list for null questionnaire toxicities`() {
        val extractor = ComorbidityExtractor(toxicityCurationDatabase, toxicityTranslationDatabase)
        assertThat(extractor.extract(PATIENT_ID, TestCurationFactory.emptyQuestionnaire(), emptyList(), emptyList()).extracted).isEmpty()
    }

    @Test
    fun `Should preferentially curate feed toxicities`() {
        assertToxicityExtraction(TOXICITY_INPUT, TOXICITY_NAME, 3, setOf(TOXICITY_INPUT.lowercase()), emptySet())
    }

    @Test
    fun `Should fall back to translation for feed toxicities when curation does not exist`() {
        assertToxicityExtraction(
            OTHER_TOXICITY_INPUT, TOXICITY_TRANSLATED, 2, emptySet(), setOf(Translation(OTHER_TOXICITY_INPUT, TOXICITY_TRANSLATED))
        )
    }

    private fun assertToxicityExtraction(
        input: String,
        expectedName: String,
        expectedGrade: Int,
        expectedCurationInputs: Set<String>,
        expectedToxicityTranslationInputs: Set<Translation<String>>
    ) {
        val inputs = setOf(input, CANNOT_CURATE).map { name ->
            DigitalFileEntry(
                subject = PATIENT_ID,
                authored = LocalDate.of(2020, 11, 11),
                itemText = name,
                itemAnswerValueValueString = "2",
                description = ""
            )
        }
        val extractor = ComorbidityExtractor(toxicityCurationDatabase, toxicityTranslationDatabase)
        val (toxicities, evaluation) = extractor.extract(PATIENT_ID, null, inputs, emptyList())
        assertThat(toxicities).hasSize(2)
            .anyMatch { it.name == expectedName && (it as Toxicity).grade == expectedGrade }

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.TOXICITY_TRANSLATION,
                CANNOT_CURATE,
                "No translation found for toxicity: '$CANNOT_CURATE'"
            )
        )
        assertThat(evaluation.comorbidityEvaluatedInputs).isEqualTo(expectedCurationInputs)
        assertThat(evaluation.toxicityTranslationEvaluatedInputs).isEqualTo(expectedToxicityTranslationInputs)
    }
}