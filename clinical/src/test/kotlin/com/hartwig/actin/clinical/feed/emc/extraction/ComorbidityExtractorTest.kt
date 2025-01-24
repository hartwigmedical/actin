package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.algo.icd.IcdConstants.ALLERGIC_OR_HYPERSENSITIVITY_CONDITIONS_OF_UNSPECIFIED_TYPE
import com.hartwig.actin.algo.icd.IcdConstants.HARMFUL_EFFECTS_OF_DRUGS_CODE
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.clinical.curation.config.ToxicityCuration
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
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
private const val COMPLICATION_INPUT = "term"
private const val COMPLICATION_ICD = "code"
private const val CURATED_COMPLICATION = "Curated"
private const val OTHER_CONDITION_INPUT = "Non-oncological input"
private const val OTHER_CONDITION_INTERPRETATION = "Prior condition interpretation"
private const val OTHER_CONDITION_ICD = "icd"
private const val INTOLERANCE_INPUT = "Intolerance input"
private const val CURATED_INTOLERANCE = "Curated intolerance"
private const val INTOLERANCE_ICD = "ICD"
private const val TOXICITY_INPUT = "Toxicity input"
private const val OTHER_TOXICITY_INPUT = "Other toxicity input"
private const val TOXICITY_NAME = "Toxicity name"
private const val TOXICITY_ICD_CODE = "Toxicity icd code"
private const val TOXICITY_EXTENSION_CODE = "Toxicity extension"
private const val TOXICITY_TRANSLATED = "Toxicity translated"

class ComorbidityExtractorTest {
    private val complicationCurationDatabase = TestCurationFactory.curationDatabase(
        ComorbidityConfig(
            input = COMPLICATION_INPUT,
            ignore = false,
            curated = Complication(name = CURATED_COMPLICATION, year = null, month = null, icdCodes = setOf(IcdCode(COMPLICATION_ICD)))
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
        val (comorbidities, evaluation) = extractor.extract(PATIENT_ID, questionnaire, emptyList(), emptyList())
        assertThat(comorbidities).isEmpty()
        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.comorbidityEvaluatedInputs).isEmpty()
    }

    @Test
    fun `Should curate complications`() {
        assertComplicationExtraction(COMPLICATION_INPUT, CURATED_COMPLICATION, COMPLICATION_ICD)
    }

    @Test
    fun `Should extract yes-input complication with empty name and ICD`() {
        assertComplicationExtraction("Yes", "", "")
    }

    private fun assertComplicationExtraction(input: String, expectedName: String, expectedIcd: String) {
        val extractor = ComorbidityExtractor(complicationCurationDatabase, toxicityTranslationDatabase)
        val inputs = listOf(input, CANNOT_CURATE)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(complications = inputs)
        val (complications, evaluation) = extractor.extract(PATIENT_ID, questionnaire, emptyList(), emptyList())
        assertThat(complications).isNotNull
        val complication = complications.single() as Complication
        assertThat(complication.name).isEqualTo(expectedName)
        assertThat(complication.icdCodes).containsExactly(IcdCode(expectedIcd))

        assertExpectedEvaluation(
            evaluation,
            CurationCategory.COMPLICATION,
            "Could not find complication config for input '$CANNOT_CURATE'",
            inputs.map(String::lowercase).toSet()
        )
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
        // TODO - handle yes inputs
        val extractor = ComorbidityExtractor(
            TestCurationFactory.curationDatabase(
                ComorbidityConfig(
                    input = OTHER_CONDITION_INPUT,
                    ignore = false,
                    lvef = null,
                    curated = OtherCondition(
                        name = OTHER_CONDITION_INTERPRETATION,
                        icdCodes = setOf(IcdCode(OTHER_CONDITION_ICD))
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

        assertExpectedEvaluation(
            evaluation,
            CurationCategory.NON_ONCOLOGICAL_HISTORY,
            "Could not find non-oncological history config for input '$CANNOT_CURATE'",
            inputs.map(String::lowercase).toSet()
        )
    }

    @Test
    fun `Should extract curated intolerances`() {
        assertIntoleranceExtraction(INTOLERANCE_INPUT, CURATED_INTOLERANCE, INTOLERANCE_ICD)
    }

    @Test
    fun `Should extract yes-input intolerances with default ICD`() {
        assertIntoleranceExtraction("YES", "", ALLERGIC_OR_HYPERSENSITIVITY_CONDITIONS_OF_UNSPECIFIED_TYPE)
    }

    private fun assertIntoleranceExtraction(input: String, expectedName: String, expectedIcd: String) {
        val medicationIntoleranceInput = "Paracetamol"
        val curatedMedicationIntolerance = "Paracetamol"
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
                    input = INTOLERANCE_INPUT,
                    ignore = false,
                    curated = Intolerance(name = CURATED_INTOLERANCE, icdCodes = setOf(IcdCode(INTOLERANCE_ICD)))
                ),
                ComorbidityConfig(
                    input = medicationIntoleranceInput,
                    ignore = false,
                    curated = Intolerance(name = curatedMedicationIntolerance, icdCodes = setOf(IcdCode(INTOLERANCE_ICD)))
                )
            ),
            toxicityTranslationDatabase
        )
        val inputs = listOf(input, cannotCurate)
        val intoleranceEntries = inputs.map { entry.copy(codeText = it) }
        val (intolerances, evaluation) =
            extractor.extract(PATIENT_ID, TestCurationFactory.emptyQuestionnaire(), emptyList(), intoleranceEntries)
        assertThat(intolerances).hasSize(2)
        val curated = intolerances.first()
        assertThat(curated.name).isEqualTo(expectedName)
        assertThat(curated.icdCodes).containsExactly(IcdCode(expectedIcd))

        assertThat(intolerances[1].name).isEqualTo(cannotCurate)

        assertExpectedEvaluation(
            evaluation,
            CurationCategory.INTOLERANCE,
            "Could not find intolerance config for input 'Cannot curate'",
            inputs.map(String::lowercase).toSet(),
            cannotCurate
        )
    }

    @Test
    fun `Should curate and translate questionnaire toxicities`() {
        assertExtractedQuestionnaireToxicity(
            listOf(TOXICITY_INPUT, CANNOT_CURATE), TOXICITY_NAME, IcdCode(TOXICITY_ICD_CODE, TOXICITY_EXTENSION_CODE), 3
        )
    }

    @Test
    fun `Should return empty list for null questionnaire toxicities`() {
        val extractor = ComorbidityExtractor(toxicityCurationDatabase, toxicityTranslationDatabase)
        assertThat(extractor.extract(PATIENT_ID, TestCurationFactory.emptyQuestionnaire(), emptyList(), emptyList()).extracted).isEmpty()
    }

    @Test
    fun `Should extract yes-input questionnaire toxicities with default ICD code`() {
        assertExtractedQuestionnaireToxicity(listOf("YES", CANNOT_CURATE), "", IcdCode(HARMFUL_EFFECTS_OF_DRUGS_CODE), null)
    }

    private fun assertExtractedQuestionnaireToxicity(
        inputs: List<String>, expectedName: String, expectedIcd: IcdCode, expectedGrade: Int?
    ) {
        val date = LocalDate.of(2018, 5, 21)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(date = date, unresolvedToxicities = inputs)
        val extractor = ComorbidityExtractor(toxicityCurationDatabase, toxicityTranslationDatabase)
        val (toxicities, evaluation) = extractor.extract(PATIENT_ID, questionnaire, emptyList(), emptyList())

        val toxicity = toxicities.single() as Toxicity
        assertThat(toxicity.name).isEqualTo(expectedName)
        assertThat(toxicity.icdCodes).containsExactly(expectedIcd)
        assertThat(toxicity.evaluatedDate).isEqualTo(date)
        assertThat(toxicity.source).isEqualTo(ToxicitySource.QUESTIONNAIRE)
        assertThat(toxicity.grade).isEqualTo(expectedGrade)
        val expectedInputs = inputs.map(String::lowercase).toSet()
        assertExpectedEvaluation(
            evaluation, CurationCategory.TOXICITY, "Could not find toxicity config for input '$CANNOT_CURATE'", expectedInputs
        )
    }

    @Test
    fun `Should preferentially curate feed toxicities`() {
        assertFeedToxicityExtraction(
            TOXICITY_INPUT,
            TOXICITY_NAME,
            3,
            IcdCode(TOXICITY_ICD_CODE, TOXICITY_EXTENSION_CODE),
            setOf(TOXICITY_INPUT.lowercase()),
            emptySet()
        )
    }

    @Test
    fun `Should fall back to translation for feed toxicities when curation does not exist`() {
        assertFeedToxicityExtraction(
            OTHER_TOXICITY_INPUT,
            TOXICITY_TRANSLATED,
            2,
            IcdCode(HARMFUL_EFFECTS_OF_DRUGS_CODE),
            emptySet(),
            setOf(Translation(OTHER_TOXICITY_INPUT, TOXICITY_TRANSLATED))
        )
    }

    @Test
    fun `Should curate yes-input feed toxicities with default ICD code`() {
        assertFeedToxicityExtraction("YES", "", 2, IcdCode(HARMFUL_EFFECTS_OF_DRUGS_CODE), setOf("yes"), emptySet())
    }

    private fun assertFeedToxicityExtraction(
        input: String,
        expectedName: String,
        expectedGrade: Int,
        expectedIcdCode: IcdCode,
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
        val expectedToxicity = toxicities.single { it.name == expectedName } as Toxicity
        assertThat(expectedToxicity.grade).isEqualTo(expectedGrade)
        assertThat(expectedToxicity.icdCodes).containsExactly(expectedIcdCode)

        assertExpectedEvaluation(
            evaluation,
            CurationCategory.TOXICITY_TRANSLATION,
            "No translation found for toxicity: '$CANNOT_CURATE'",
            expectedCurationInputs
        )
        assertThat(evaluation.toxicityTranslationEvaluatedInputs).isEqualTo(expectedToxicityTranslationInputs)
    }

    private fun assertExpectedEvaluation(
        evaluation: CurationExtractionEvaluation,
        category: CurationCategory,
        message: String,
        expectedInputs: Set<String>,
        cannotCurate: String = CANNOT_CURATE
    ) {
        assertThat(evaluation.warnings).containsOnly(CurationWarning(PATIENT_ID, category, cannotCurate, message))
        assertThat(evaluation.comorbidityEvaluatedInputs).isEqualTo(expectedInputs)
    }
}