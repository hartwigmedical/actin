package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import org.junit.Test
import java.time.LocalDate

val referenceDate = LocalDate.of(2024, 10, 1)

class HasToxicityWithGradeTest {

    @Test
    fun canEvaluateGradeOnly() {
        val function = function()
        val toxicities: MutableList<Toxicity> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
        toxicities.add(ToxicityTestFactory.toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 1))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
        toxicities.add(ToxicityTestFactory.toxicity(source = ToxicitySource.EHR, grade = 2))
        assertEvaluation(EvaluationResult.WARN, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
        toxicities.add(ToxicityTestFactory.toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    @Test
    fun canEvaluateQuestionnaireToxicityWithoutGrade() {
        val toxicities: MutableList<Toxicity> = mutableListOf()
        toxicities.add(ToxicityTestFactory.toxicity(source = ToxicitySource.QUESTIONNAIRE))
        val match = function()
        assertEvaluation(EvaluationResult.PASS, match.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
        val noMatch = function(minGrade = DEFAULT_QUESTIONNAIRE_GRADE + 1)
        assertEvaluation(EvaluationResult.UNDETERMINED, noMatch.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
        toxicities.add(
            ToxicityTestFactory.toxicity(
                source = ToxicitySource.QUESTIONNAIRE, grade = DEFAULT_QUESTIONNAIRE_GRADE + 2
            )
        )
        assertEvaluation(EvaluationResult.PASS, noMatch.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    @Test
    fun canIgnoreToxicities() {
        val function = function(ignoreFilters = setOf("ignore"))
        val toxicities: MutableList<Toxicity> = mutableListOf()
        toxicities.add(ToxicityTestFactory.toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2, name = "ignore me please"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
        toxicities.add(ToxicityTestFactory.toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2, name = "keep me please"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    @Test
    fun canFilterOnSpecificToxicity() {
        val function = function(nameFilter = "specific")
        val toxicities: MutableList<Toxicity> = mutableListOf()
        toxicities.add(ToxicityTestFactory.toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2, name = "something random"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
        toxicities.add(ToxicityTestFactory.toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2, name = "something specific"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    @Test
    fun picksOnlyMostRecentEHRToxicities() {
        val function = function()
        val toxicities: MutableList<Toxicity> = mutableListOf()
        toxicities.add(
            ToxicityTestFactory.toxicity(
                source = ToxicitySource.EHR,
                grade = 2,
                name = "toxicity 1",
                evaluatedDate = LocalDate.of(2020, 1, 1)
            )
        )
        assertEvaluation(EvaluationResult.WARN, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
        toxicities.add(
            ToxicityTestFactory.toxicity(
                source = ToxicitySource.EHR,
                grade = 1,
                name = "toxicity 1",
                evaluatedDate = LocalDate.of(2021, 1, 1)
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
        toxicities.add(
            ToxicityTestFactory.toxicity(
                source = ToxicitySource.EHR,
                grade = 3,
                name = "toxicity 1",
                evaluatedDate = LocalDate.of(2022, 1, 1)
            )
        )
        assertEvaluation(EvaluationResult.WARN, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    @Test
    fun ignoresEHRToxicitiesThatAreAlsoComplications() {
        val function = function()
        val questionnaireToxicity: Toxicity = ToxicityTestFactory.toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2)
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(ToxicityTestFactory.withToxicityThatIsAlsoComplication(questionnaireToxicity))
        )
        val ehrToxicity: Toxicity = ToxicityTestFactory.toxicity(source = ToxicitySource.EHR, grade = 2)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicityThatIsAlsoComplication(ehrToxicity)))
    }

    @Test
    fun returnsRecoverableWarnWhenQuestionnaireIsNotSource() {
        val function = function()
        val toxicities = listOf(
            ToxicityTestFactory.toxicity(
                source = ToxicitySource.EHR,
                grade = DEFAULT_QUESTIONNAIRE_GRADE,
                name = "toxicity 1",
                evaluatedDate = LocalDate.of(2020, 1, 1)
            )
        )
        assertEvaluation(EvaluationResult.WARN, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    @Test
    fun returnsRecoverablePassWhenQuestionnaireIsNotSourceButConfiguredNotToWarn() {
        val function = function(warnIfToxicitiesNotFromQuestionnaire = false)
        val toxicities = listOf(
            ToxicityTestFactory.toxicity(
                source = ToxicitySource.EHR,
                grade = DEFAULT_QUESTIONNAIRE_GRADE,
                name = "toxicity 1",
                evaluatedDate = LocalDate.of(2020, 1, 1)
            )
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    @Test
    fun `Should not pass for toxicity with earlier end date`() {
        val function = function()
        val toxicities = listOf(
            Toxicity(
                source = ToxicitySource.QUESTIONNAIRE,
                grade = DEFAULT_QUESTIONNAIRE_GRADE,
                name = "toxicity 1",
                evaluatedDate = LocalDate.of(2020, 1, 1),
                endDate = LocalDate.of(2022, 1, 2),
                categories = emptySet()
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    private fun function(
        minGrade: Int = DEFAULT_QUESTIONNAIRE_GRADE,
        nameFilter: String? = null,
        ignoreFilters: Set<String> = emptySet(),
        warnIfToxicitiesNotFromQuestionnaire: Boolean = true
    ): HasToxicityWithGrade {
        return HasToxicityWithGrade(
            minGrade, nameFilter, ignoreFilters, warnIfToxicitiesNotFromQuestionnaire, referenceDate
        )
    }
}