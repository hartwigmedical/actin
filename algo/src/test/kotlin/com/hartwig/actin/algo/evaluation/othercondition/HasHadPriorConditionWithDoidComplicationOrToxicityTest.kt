package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val OTHER_CONDITION_NAME: String = "other condition"
private const val DOID: String = "1234"
private const val TOXICITY_CATEGORY: String = "toxicity_category"
private const val COMPLICATION_CATEGORY: String = "complication_category"
private const val COMPLICATION_NAME: String = "complication"
private const val TOXICITY_NAME: String = "toxicity"
private const val DOID_TERM: String = "some disease"

class HasHadPriorConditionWithDoidComplicationOrToxicityTest {
    private val function = HasHadPriorConditionWithDoidComplicationOrToxicity(
        TestDoidModelFactory.createWithOneDoidAndTerm(DOID, DOID_TERM), DOID, COMPLICATION_CATEGORY, TOXICITY_CATEGORY
    )

    private val minimalPatient = TestPatientFactory.createMinimalTestWGSPatientRecord()

    private val complication = OtherConditionTestFactory.complication(
        categories = setOf(COMPLICATION_CATEGORY),
        name = COMPLICATION_NAME
    )

    private val priorOtherCondition = OtherConditionTestFactory.priorOtherCondition(
        doids = setOf(DOID),
        name = OTHER_CONDITION_NAME,
        category = DOID_TERM,
        isContraindication = true
    )

    @Test
    fun `Should evaluate fail when doid not present in model`() {
        val function = HasHadPriorConditionWithDoidComplicationOrToxicity(
            TestDoidModelFactory.createMinimalTestDoidModel(),
            DOID,
            COMPLICATION_NAME,
            TOXICITY_NAME
        )
        val evaluation = function.evaluate(minimalPatient)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessages).containsOnly("Has no other condition belonging to category unknown doid")
    }

    @Test
    fun `Should evaluate fail when no matching doid complication or toxicity`() {
        val evaluation = function.evaluate(minimalPatient)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessages).containsOnly("Has no other condition belonging to category some disease")
    }

    @Test
    fun `Should evaluate pass when doid term matches category`() {
        assertPassEvaluationWithMessages(
            function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(priorOtherCondition)),
            message("condition(s)", "other condition")
        )
    }

    @Test
    fun `Should evaluate pass when complication matches category`() {
        assertPassEvaluationWithMessages(
            function.evaluate(OtherConditionTestFactory.withComplications(listOf(complication))),
            message("complication(s)", "complication")
        )
    }

    @Test
    fun `Should evaluate pass when toxicity from questionnaire matches category`() {
        assertPassEvaluationWithMessages(
            function.evaluate(OtherConditionTestFactory.withToxicities(listOf(toxicity(ToxicitySource.QUESTIONNAIRE, 1)))),
            message("toxicity(ies)", "toxicity")
        )
    }

    @Test
    fun `Should evaluate pass when toxicity with at least grade two matches category`() {
        assertPassEvaluationWithMessages(
            function.evaluate(OtherConditionTestFactory.withToxicities(listOf(toxicity(ToxicitySource.EHR, 2)))),
            message("toxicity(ies)", "toxicity"),
        )
    }

    @Test
    fun `Should evaluate fail when toxicity less than grade two and source not questionnaire`() {
        val evaluation = function.evaluate(OtherConditionTestFactory.withToxicities(listOf(toxicity(ToxicitySource.EHR, 1))))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessages).containsOnly("Has no other condition belonging to category some disease")
    }

    @Test
    fun `Should include multiple messages`() {
        assertPassEvaluationWithMessages(
            function.evaluate(
                minimalPatient.copy(
                    toxicities = listOf(toxicity(ToxicitySource.QUESTIONNAIRE, 2)),
                    complications = listOf(complication),
                    priorOtherConditions = listOf(priorOtherCondition)
                )
            ),
            message("condition(s)", "other condition"),
            message("complication(s)", "complication"),
            message("toxicity(ies)", "toxicity")
        )
    }

    private fun toxicity(toxicitySource: ToxicitySource, grade: Int?): Toxicity {
        return Toxicity(
            categories = setOf(TOXICITY_CATEGORY),
            name = TOXICITY_NAME,
            evaluatedDate = LocalDate.now(),
            source = toxicitySource,
            grade = grade
        )
    }

    private fun message(type: String, matchedNames: String): String {
        return "Has history of $type $matchedNames which is indicative of some disease"
    }

    private fun assertPassEvaluationWithMessages(evaluation: Evaluation, vararg passMessages: String) {
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passMessages).containsOnly(*passMessages)
    }
}