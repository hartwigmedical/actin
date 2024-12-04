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
        assertThat(evaluation.failSpecificMessages)
            .containsOnly("Patient has no other condition belonging to category unknown doid")
        assertThat(evaluation.failGeneralMessages).containsOnly("No relevant non-oncological condition")
    }

    @Test
    fun `Should evaluate fail when no matching doid complication or toxicity`() {
        val evaluation = function.evaluate(minimalPatient)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failSpecificMessages)
            .containsOnly("Patient has no other condition belonging to category some disease")
        assertThat(evaluation.failGeneralMessages).containsOnly("No relevant non-oncological condition")
    }

    @Test
    fun `Should evaluate pass when doid term matches category`() {
        assertPassEvaluationWithMessages(
            function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(priorOtherCondition)),
            "other condition",
            "Patient has history of condition(s) other condition, which is indicative of some disease"
        )
    }

    @Test
    fun `Should evaluate pass when complication matches category`() {
        assertPassEvaluationWithMessages(
            function.evaluate(OtherConditionTestFactory.withComplications(listOf(complication))),
            "complication",
            "Patient has history of complication(s) complication, which is indicative of some disease"
        )
    }

    @Test
    fun `Should evaluate pass when toxicity from questionnaire matches category`() {
        assertPassEvaluationWithMessages(
            function.evaluate(OtherConditionTestFactory.withToxicities(listOf(toxicity(ToxicitySource.QUESTIONNAIRE, 1)))),
            "toxicity",
            "Patient has history of toxicity(ies) toxicity, which is indicative of some disease"
        )
    }

    @Test
    fun `Should evaluate pass when toxicity with at least grade two matches category`() {
        assertPassEvaluationWithMessages(
            function.evaluate(OtherConditionTestFactory.withToxicities(listOf(toxicity(ToxicitySource.EHR, 2)))),
            "toxicity",
            "Patient has history of toxicity(ies) toxicity, which is indicative of some disease"
        )
    }

    @Test
    fun `Should evaluate fail when toxicity less than grade two and source not questionnaire`() {
        val evaluation = function.evaluate(OtherConditionTestFactory.withToxicities(listOf(toxicity(ToxicitySource.EHR, 1))))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failSpecificMessages)
            .containsOnly("Patient has no other condition belonging to category some disease")
        assertThat(evaluation.failGeneralMessages).containsOnly("No relevant non-oncological condition")
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
            "complication and other condition and toxicity",
            "Patient has history of toxicity(ies) toxicity, which is indicative of some disease",
            "Patient has history of complication(s) complication, which is indicative of some disease",
            "Patient has history of condition(s) other condition, which is indicative of some disease"
        )
    }

    private fun toxicity(toxicitySource: ToxicitySource, grade: Int?): Toxicity {
        return Toxicity(
            categories = setOf(TOXICITY_CATEGORY),
            icdCode = "code",
            name = TOXICITY_NAME,
            evaluatedDate = LocalDate.now(),
            source = toxicitySource,
            grade = grade
        )
    }

    private fun assertPassEvaluationWithMessages(evaluation: Evaluation, matchedNames: String, vararg passSpecificMessages: String) {
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passSpecificMessages).containsOnly(*passSpecificMessages)
        assertThat(evaluation.passGeneralMessages).containsOnly("History of $matchedNames")
    }
}