package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import org.junit.Test
import java.time.LocalDate

class HasToxicityWithGradeTest {

    @Test
    fun canEvaluateGradeOnly() {
        val function = HasToxicityWithGrade(2, null, emptySet())
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
        val match = HasToxicityWithGrade(HasToxicityWithGrade.DEFAULT_QUESTIONNAIRE_GRADE, null, emptySet())
        assertEvaluation(EvaluationResult.PASS, match.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
        val noMatch = HasToxicityWithGrade(HasToxicityWithGrade.DEFAULT_QUESTIONNAIRE_GRADE + 1, null, emptySet())
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
        val function = HasToxicityWithGrade(2, null, setOf("ignore"))
        val toxicities: MutableList<Toxicity> = mutableListOf()
        toxicities.add(ToxicityTestFactory.toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2, name = "ignore me please"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
        toxicities.add(ToxicityTestFactory.toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2, name = "keep me please"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    @Test
    fun canFilterOnSpecificToxicity() {
        val function = HasToxicityWithGrade(2, "specific", emptySet())
        val toxicities: MutableList<Toxicity> = mutableListOf()
        toxicities.add(ToxicityTestFactory.toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2, name = "something random"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
        toxicities.add(ToxicityTestFactory.toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2, name = "something specific"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    @Test
    fun picksOnlyMostRecentEHRToxicities() {
        val function = HasToxicityWithGrade(2, null, emptySet())
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
        val function = HasToxicityWithGrade(2, null, emptySet())
        val questionnaireToxicity: Toxicity = ToxicityTestFactory.toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2)
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(ToxicityTestFactory.withToxicityThatIsAlsoComplication(questionnaireToxicity))
        )
        val ehrToxicity: Toxicity = ToxicityTestFactory.toxicity(source = ToxicitySource.EHR, grade = 2)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicityThatIsAlsoComplication(ehrToxicity)))
    }
}