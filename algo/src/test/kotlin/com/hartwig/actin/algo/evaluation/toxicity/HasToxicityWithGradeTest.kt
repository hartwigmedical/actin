package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import com.hartwig.actin.configuration.AlgoConfiguration
import com.hartwig.actin.configuration.EnvironmentConfiguration
import java.time.LocalDate
import org.junit.Test

class HasToxicityWithGradeTest {

    @Test
    fun canEvaluateGradeOnly() {
        val function = victimise()
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
        val match = victimise()
        assertEvaluation(EvaluationResult.PASS, match.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
        val noMatch = victimise(minGrade = HasToxicityWithGrade.DEFAULT_QUESTIONNAIRE_GRADE + 1)
        assertEvaluation(EvaluationResult.UNDETERMINED, noMatch.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
        toxicities.add(
            ToxicityTestFactory.toxicity(
                source = ToxicitySource.QUESTIONNAIRE, grade = HasToxicityWithGrade.DEFAULT_QUESTIONNAIRE_GRADE + 2
            )
        )
        assertEvaluation(EvaluationResult.PASS, noMatch.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    @Test
    fun canIgnoreToxicities() {
        val function = victimise(ignoreFilters = setOf("ignore"))
        val toxicities: MutableList<Toxicity> = mutableListOf()
        toxicities.add(ToxicityTestFactory.toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2, name = "ignore me please"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
        toxicities.add(ToxicityTestFactory.toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2, name = "keep me please"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    @Test
    fun canFilterOnSpecificToxicity() {
        val function = victimise(nameFilter = "specific")
        val toxicities: MutableList<Toxicity> = mutableListOf()
        toxicities.add(ToxicityTestFactory.toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2, name = "something random"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
        toxicities.add(ToxicityTestFactory.toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2, name = "something specific"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    @Test
    fun picksOnlyMostRecentEHRToxicities() {
        val function = victimise()
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
        val function = victimise()
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
        val function = victimise()
        val toxicities: MutableList<Toxicity> = mutableListOf()
        toxicities.add(
            ToxicityTestFactory.toxicity(
                source = ToxicitySource.EHR,
                grade = HasToxicityWithGrade.DEFAULT_QUESTIONNAIRE_GRADE,
                name = "toxicity 1",
                evaluatedDate = LocalDate.of(2020, 1, 1)
            )
        )
        assertEvaluation(EvaluationResult.WARN, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    @Test
    fun returnsRecoverablePassWhenQuestionnaireIsNotSourceButConfiguredNotToWarn() {
        val function = victimise(config = EnvironmentConfiguration().algo.copy(warnIfToxicitiesNotFromQuestionnaire = false))
        val toxicities: MutableList<Toxicity> = mutableListOf()
        toxicities.add(
            ToxicityTestFactory.toxicity(
                source = ToxicitySource.EHR,
                grade = HasToxicityWithGrade.DEFAULT_QUESTIONNAIRE_GRADE,
                name = "toxicity 1",
                evaluatedDate = LocalDate.of(2020, 1, 1)
            )
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    private fun victimise(
        minGrade: Int = HasToxicityWithGrade.DEFAULT_QUESTIONNAIRE_GRADE,
        nameFilter: String? = null,
        ignoreFilters: Set<String> = emptySet(),
        config: AlgoConfiguration = EnvironmentConfiguration().algo
    ): HasToxicityWithGrade {
        return HasToxicityWithGrade(minGrade, nameFilter, ignoreFilters, config)
    }
}