package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.algo.icd.IcdConstants.ALLERGIC_OR_HYPERSENSITIVITY_CONDITIONS_OF_UNSPECIFIED_TYPE
import com.hartwig.actin.algo.icd.IcdConstants.HARMFUL_EFFECTS_OF_DRUGS_CODE
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.clinical.curation.config.ToxicityCuration
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.Translation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.feed.emc.digitalfile.DigitalFileEntry
import com.hartwig.actin.clinical.feed.emc.intolerance.IntoleranceEntry
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.Ecg
import com.hartwig.actin.datamodel.clinical.EcgMeasure
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.InfectionStatus
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
private const val TOXICITY_TRANSLATED = "Toxicity translated"
private const val CURATED_LVEF = 1.0

class ComorbidityExtractorTest {
    private val complicationCurationDatabase = TestCurationFactory.curationDatabase(
        ComorbidityConfig(
            input = COMPLICATION_INPUT,
            ignore = false,
            curated = Complication(name = CURATED_COMPLICATION, icdCodes = setOf(IcdCode(COMPLICATION_ICD)))
        ),
        ComorbidityConfig(input = "none", ignore = true, curated = null)
    )

    private val toxicityIcdCodes = setOf(IcdCode("Toxicity icd code", "Toxicity extension"))

    private val toxicityCurationDatabase = TestCurationFactory.curationDatabase(
        ComorbidityConfig(
            input = TOXICITY_INPUT,
            ignore = false,
            curated = ToxicityCuration(
                name = TOXICITY_NAME,
                grade = 3,
                icdCodes = toxicityIcdCodes
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
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(nonOncologicalHistory = input)
        val (extraction, evaluation) = extractor.extract(PATIENT_ID, questionnaire, emptyList(), emptyList())
        val (comorbidities, clinicalStatus) = extraction
        assertThat(comorbidities).isEmpty()
        assertThat(clinicalStatus).isEqualTo(ClinicalStatus())
        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.comorbidityEvaluatedInputs).isEmpty()
    }

    @Test
    fun `Should curate complications`() {
        assertComplicationExtraction(COMPLICATION_INPUT, CURATED_COMPLICATION, COMPLICATION_ICD)
    }

    @Test
    fun `Should extract yes-input complication with empty name and ICD`() {
        assertComplicationExtraction("JA", null, null)
    }

    private fun assertComplicationExtraction(input: String, expectedName: String?, expectedIcd: String?) {
        val extractor = ComorbidityExtractor(complicationCurationDatabase, toxicityTranslationDatabase)
        val inputs = listOf(input, CANNOT_CURATE)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(complications = inputs)
        val (extraction, evaluation) = extractor.extract(PATIENT_ID, questionnaire, emptyList(), emptyList())
        val complication = extraction.first.single() as Complication
        assertThat(complication.name).isEqualTo(expectedName)
        assertThat(complication.icdCodes).isEqualTo(setOfNotNull(expectedIcd?.let { IcdCode(it) }))

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
        assertThat(ignore.first).isEmpty()
        assertThat(evaluation.warnings).isEmpty()
        assertThat(evaluation.comorbidityEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())
    }

    @Test
    fun `Should curate other conditions`() {
        assertOtherConditionExtraction(OTHER_CONDITION_INPUT, OTHER_CONDITION_INTERPRETATION, OTHER_CONDITION_ICD)
    }

    @Test
    fun `Should extract yes-input other condition with empty name and ICD`() {
        assertOtherConditionExtraction("ye", null, null)
    }

    private fun assertOtherConditionExtraction(input: String, expectedName: String?, expectedIcd: String?) {
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
        val inputs = listOf(input, CANNOT_CURATE)
        val questionnaire = TestCurationFactory.emptyQuestionnaire()
            .copy(nonOncologicalHistory = inputs)
        val (extraction, evaluation) = extractor.extract(PATIENT_ID, questionnaire, emptyList(), emptyList())
        val otherCondition = extraction.first.single() as OtherCondition
        assertThat(otherCondition.name).isEqualTo(expectedName)
        assertThat(otherCondition.icdCodes).isEqualTo(setOfNotNull(expectedIcd?.let { IcdCode(it) }))

        assertExpectedEvaluation(
            evaluation,
            CurationCategory.NON_ONCOLOGICAL_HISTORY,
            "Could not find non oncological history config for input '$CANNOT_CURATE'",
            inputs.map(String::lowercase).toSet()
        )
    }

    @Test
    fun `Should extract toxicity for other condition curated to toxicity curation`() {
        val extractor = ComorbidityExtractor(
            TestCurationFactory.curationDatabase(
                ComorbidityConfig(
                    input = OTHER_CONDITION_INPUT,
                    ignore = false,
                    curated = ToxicityCuration(
                        name = TOXICITY_NAME,
                        grade = 3,
                        icdCodes = toxicityIcdCodes
                    )
                )
            ),
            toxicityTranslationDatabase
        )
        val inputs = listOf(OTHER_CONDITION_INPUT, CANNOT_CURATE)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(nonOncologicalHistory = inputs)
        val (extraction, evaluation) = extractor.extract(PATIENT_ID, questionnaire, emptyList(), emptyList())
        val toxicity = extraction.first.single() as Toxicity
        assertThat(toxicity).isEqualTo(
            Toxicity(
                name = TOXICITY_NAME,
                icdCodes = toxicityIcdCodes,
                evaluatedDate = questionnaire.date,
                source = ToxicitySource.QUESTIONNAIRE,
                grade = 3
            )
        )

        assertExpectedEvaluation(
            evaluation,
            CurationCategory.NON_ONCOLOGICAL_HISTORY,
            "Could not find non oncological history config for input '$CANNOT_CURATE'",
            inputs.map(String::lowercase).toSet()
        )
    }

    @Test
    fun `Should extract curated intolerances`() {
        assertIntoleranceExtraction(INTOLERANCE_INPUT, CURATED_INTOLERANCE, INTOLERANCE_ICD)
    }

    @Test
    fun `Should extract yes-input intolerances with default ICD`() {
        assertIntoleranceExtraction("YES", null, ALLERGIC_OR_HYPERSENSITIVITY_CONDITIONS_OF_UNSPECIFIED_TYPE)
    }

    private fun assertIntoleranceExtraction(input: String, expectedName: String?, expectedIcd: String) {
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
        val (extraction, evaluation) =
            extractor.extract(PATIENT_ID, TestCurationFactory.emptyQuestionnaire(), emptyList(), intoleranceEntries)
        val intolerances = extraction.first
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
        assertExtractedQuestionnaireToxicity(listOf(TOXICITY_INPUT, CANNOT_CURATE), TOXICITY_NAME, toxicityIcdCodes, 3)
    }

    @Test
    fun `Should return empty list for null questionnaire toxicities`() {
        val extractor = ComorbidityExtractor(toxicityCurationDatabase, toxicityTranslationDatabase)
        assertThat(
            extractor.extract(PATIENT_ID, TestCurationFactory.emptyQuestionnaire(), emptyList(), emptyList()).extracted.first
        ).isEmpty()
    }

    @Test
    fun `Should extract yes-input questionnaire toxicities with default ICD code`() {
        assertExtractedQuestionnaireToxicity(listOf("YES", CANNOT_CURATE), null, setOf(IcdCode(HARMFUL_EFFECTS_OF_DRUGS_CODE)), null)
    }

    private fun assertExtractedQuestionnaireToxicity(
        inputs: List<String>, expectedName: String?, expectedIcds: Set<IcdCode>, expectedGrade: Int?
    ) {
        val date = LocalDate.of(2018, 5, 21)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(date = date, unresolvedToxicities = inputs)
        val extractor = ComorbidityExtractor(toxicityCurationDatabase, toxicityTranslationDatabase)
        val (extraction, evaluation) = extractor.extract(PATIENT_ID, questionnaire, emptyList(), emptyList())

        val toxicity = extraction.first.single() as Toxicity
        assertThat(toxicity.name).isEqualTo(expectedName)
        assertThat(toxicity.icdCodes).isEqualTo(expectedIcds)
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
        assertFeedToxicityExtraction(TOXICITY_INPUT, TOXICITY_NAME, 3, toxicityIcdCodes, setOf(TOXICITY_INPUT.lowercase()), emptySet())
    }

    @Test
    fun `Should fall back to translation for feed toxicities when curation does not exist`() {
        assertFeedToxicityExtraction(
            OTHER_TOXICITY_INPUT,
            TOXICITY_TRANSLATED,
            2,
            setOf(IcdCode(HARMFUL_EFFECTS_OF_DRUGS_CODE)),
            emptySet(),
            setOf(Translation(OTHER_TOXICITY_INPUT, TOXICITY_TRANSLATED))
        )
    }

    @Test
    fun `Should curate yes-input feed toxicities with default ICD code`() {
        assertFeedToxicityExtraction("YES", null, 2, setOf(IcdCode(HARMFUL_EFFECTS_OF_DRUGS_CODE)), setOf("yes"), emptySet())
    }

    private fun assertFeedToxicityExtraction(
        input: String,
        expectedName: String?,
        expectedGrade: Int,
        expectedIcdCodes: Set<IcdCode>,
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
        val (extraction, evaluation) = extractor.extract(PATIENT_ID, null, inputs, emptyList())
        assertThat(extraction.first).hasSize(2)
        val expectedToxicity = extraction.first.single { it.name == expectedName } as Toxicity
        assertThat(expectedToxicity.grade).isEqualTo(expectedGrade)
        assertThat(expectedToxicity.icdCodes).isEqualTo(expectedIcdCodes)

        assertExpectedEvaluation(
            evaluation,
            CurationCategory.TOXICITY_TRANSLATION,
            "No translation found for toxicity: '$CANNOT_CURATE'",
            expectedCurationInputs
        )
        assertThat(evaluation.toxicityTranslationEvaluatedInputs).isEqualTo(expectedToxicityTranslationInputs)
    }

    @Test
    fun `Should extract ECG from questionnaire`() {
        val date = LocalDate.of(2018, 5, 21)
        val ecgInput = "ECG input"
        val curatedEcg = "curated"
        val jtcMeasure = EcgMeasure(1, "unit")
        val icd = setOf(IcdCode("icd"))
        val rawEcg = Ecg(ecgInput, null, jtcMeasure)
        val questionnaire = TestCurationFactory.emptyQuestionnaire()
            .copy(date = date, ecg = rawEcg, nonOncologicalHistory = listOf(CANNOT_CURATE))
        val curationDatabase = TestCurationFactory.curationDatabase(
            ComorbidityConfig(
                input = ecgInput,
                ignore = false,
                curated = Ecg(name = curatedEcg, qtcfMeasure = null, jtcMeasure = jtcMeasure, icdCodes = icd)
            )
        )
        val extractor = ComorbidityExtractor(curationDatabase, toxicityTranslationDatabase)
        val (extracted, evaluation) = extractor.extract(PATIENT_ID, questionnaire, emptyList(), emptyList())

        val ecg = extracted.first.single() as Ecg
        assertThat(ecg.name).isEqualTo(curatedEcg)
        assertThat(ecg.icdCodes).isEqualTo(icd)
        assertExpectedEvaluation(
            evaluation,
            CurationCategory.NON_ONCOLOGICAL_HISTORY,
            "Could not find non oncological history config for input '$CANNOT_CURATE'",
            setOf(ecgInput.lowercase(), CANNOT_CURATE)
        )
    }

    @Test
    fun `Should extract infections as comorbidities and infection status`() {
        val icdCodes = setOf(IcdCode("icd"))
        val curatedName = "Curated"
        val infectionInput = "Infection"
        val extractor = ComorbidityExtractor(
            TestCurationFactory.curationDatabase(
                ComorbidityConfig(
                    input = infectionInput,
                    ignore = false,
                    curated = OtherCondition(curatedName, icdCodes)
                )
            ),
            toxicityTranslationDatabase
        )
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(infectionStatus = InfectionStatus(true, infectionInput))
        val (extraction, evaluation) = extractor.extract(PATIENT_ID, questionnaire, emptyList(), emptyList())

        assertThat(extraction.first).containsExactly(OtherCondition(curatedName, icdCodes))
        val expectedClinicalStatus = ClinicalStatus(infectionStatus = InfectionStatus(true, curatedName), hasComplications = null)
        assertThat(extraction.second).isEqualTo(expectedClinicalStatus)
        assertThat(evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract clinical status and curate lvef`() {
        val extractor = ComorbidityExtractor(
            TestCurationFactory.curationDatabase(
                ComorbidityConfig(input = OTHER_CONDITION_INPUT, ignore = false, lvef = CURATED_LVEF, curated = null),
                ComorbidityConfig(
                    input = COMPLICATION_INPUT,
                    ignore = false,
                    curated = Complication(name = CURATED_COMPLICATION, icdCodes = setOf(IcdCode(COMPLICATION_ICD)))
                )
            ),
            toxicityTranslationDatabase
        )
        val questionnaire = TestCurationFactory.emptyQuestionnaire()
            .copy(whoStatus = 1, nonOncologicalHistory = listOf(OTHER_CONDITION_INPUT), complications = listOf(COMPLICATION_INPUT))
        val (extraction, evaluation) = extractor.extract(PATIENT_ID, questionnaire, emptyList(), emptyList())
        val (comorbidities, clinicalStatus) = extraction
        assertThat(comorbidities).containsExactly(Complication(CURATED_COMPLICATION, setOf(IcdCode(COMPLICATION_ICD))))
        assertThat(clinicalStatus.who).isEqualTo(1)
        assertThat(clinicalStatus.lvef).isEqualTo(CURATED_LVEF)
        assertThat(clinicalStatus.infectionStatus).isNull()
        assertThat(clinicalStatus.hasComplications).isTrue
        assertThat(evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract clinical status with hasComplications == null when questionnaire indicates complications are unknown`() {
        extractAndAssertComplicationStatus("unknown", null)
    }

    @Test
    fun `Should extract clinical status with hasComplications == false when questionnaire indicates no complications`() {
        extractAndAssertComplicationStatus("no", false)
    }

    @Test
    fun `Should extract clinical status with hasComplications == false when questionnaire contains empty list of complications`() {
        extractAndAssertComplicationStatus(null, false)
    }

    private fun extractAndAssertComplicationStatus(input: String?, expected: Boolean?) {
        val configs = listOfNotNull(input?.let { ComorbidityConfig(input = it, ignore = true, curated = null) }).toTypedArray()
        val extractor = ComorbidityExtractor(TestCurationFactory.curationDatabase(*configs), toxicityTranslationDatabase)
        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(complications = listOfNotNull(input))
        val (extraction, evaluation) = extractor.extract(PATIENT_ID, questionnaire, emptyList(), emptyList())
        assertThat(extraction.second.hasComplications).isEqualTo(expected)
        assertThat(evaluation.warnings).isEmpty()
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