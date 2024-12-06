package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val OTHER_CONDITION_NAME: String = "other condition"
private const val COMPLICATION_NAME: String = "complication"
private const val TOXICITY_NAME: String = "toxicity"
private const val childCode = "childCode"
private const val parentCode = "childParentCode"
private const val targetTitle = "childParentTitle"
private const val diseaseDescription = "parent disease"

class HasHadPriorConditionComplicationOrToxicityWithIcdCodeTest {
    private val icdModel = TestIcdFactory.createModelWithSpecificNodes(listOf("child", "otherTarget", "childParent"))
    private val referenceDate = LocalDate.of(2024, 12, 6)
    private val function = HasHadPriorConditionComplicationOrToxicityWithIcdCode(
        icdModel, listOf(targetTitle), diseaseDescription, referenceDate
    )
    private val minimalPatient = TestPatientFactory.createMinimalTestWGSPatientRecord()

    private val complicationWithTargetCode = OtherConditionTestFactory.complication(icdCode = parentCode, name = COMPLICATION_NAME)
    private val complicationWithChildOfTargetCode = complicationWithTargetCode.copy(icdCode = childCode)

    private val conditionWithTargetCode = OtherConditionTestFactory.priorOtherCondition(
        icdCode = parentCode,
        name = OTHER_CONDITION_NAME,
        isContraindication = true
    )
    private val conditionWithChildOfTargetCode = conditionWithTargetCode.copy(icdCode = childCode)

    @Test
    fun `Should fail when no matching icd code in prior other conditions, complications or toxicities`() {
        val evaluation = function.evaluate(minimalPatient)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failSpecificMessages)
            .containsOnly("Patient has no other condition belonging to category $diseaseDescription")
        assertThat(evaluation.failGeneralMessages).containsOnly("No relevant non-oncological condition")
    }

    @Test
    fun `Should pass when icd code or parent code of other condition matches code of target title`() {
        listOf(conditionWithTargetCode, conditionWithChildOfTargetCode).forEach {
            assertPassEvaluationWithMessages(
                function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(it)),
                "other condition",
                "Patient has history of condition(s) other condition, which is indicative of $diseaseDescription"
            )
        }
    }

    @Test
    fun `Should pass when icd code or parent code of complication matches code of target title`() {
        listOf(complicationWithChildOfTargetCode, complicationWithTargetCode).forEach {
            assertPassEvaluationWithMessages(
                function.evaluate(OtherConditionTestFactory.withComplications(listOf(it))),
                "complication",
                "Patient has history of complication(s) complication, which is indicative of $diseaseDescription"
            )
        }
    }

    @Test
    fun `Should pass when icd code or parent code of toxicity from questionnaire matches code of target title`() {
        listOf(childCode, parentCode).forEach {
            assertPassEvaluationWithMessages(
                function.evaluate(OtherConditionTestFactory.withToxicities(listOf(toxicity(ToxicitySource.QUESTIONNAIRE, it, 1)))),
                "toxicity",
                "Patient has history of toxicity(ies) toxicity, which is indicative of $diseaseDescription"
            )
        }
    }

    @Test
    fun `Should pass when icd code or parent code of toxicity from EHR with at least grade 2 matches code of target title`() {
        listOf(childCode, parentCode).forEach {
            assertPassEvaluationWithMessages(
                function.evaluate(OtherConditionTestFactory.withToxicities(listOf(toxicity(ToxicitySource.EHR, it, 2)))),
                "toxicity",
                "Patient has history of toxicity(ies) toxicity, which is indicative of $diseaseDescription"
            )
        }
    }

    @Test
    fun `Should fail when icd code or parent code of toxicity from EHR with grade less than 2 matches code of target title`() {
        listOf(childCode, parentCode).forEach {
            assertEvaluation(
                EvaluationResult.FAIL,
                function.evaluate(
                    OtherConditionTestFactory.withToxicities(
                        listOf(toxicity(ToxicitySource.EHR, it, 1))
                    )
                )
            )
        }
    }

    @Test
    fun `Should include multiple messages`() {
        assertPassEvaluationWithMessages(
            function.evaluate(
                minimalPatient.copy(
                    toxicities = listOf(toxicity(ToxicitySource.QUESTIONNAIRE, childCode, 2)),
                    complications = listOf(complicationWithTargetCode),
                    priorOtherConditions = listOf(conditionWithTargetCode)
                )
            ),
            "complication and other condition and toxicity",
            "Patient has history of toxicity(ies) toxicity, which is indicative of $diseaseDescription",
            "Patient has history of complication(s) complication, which is indicative of $diseaseDescription",
            "Patient has history of condition(s) other condition, which is indicative of $diseaseDescription"
        )
    }

    private fun toxicity(toxicitySource: ToxicitySource, icdCode: String, grade: Int?): Toxicity {
        return Toxicity(
            icdCode = icdCode,
            name = TOXICITY_NAME,
            evaluatedDate = referenceDate,
            source = toxicitySource,
            grade = grade,
            categories = emptySet()
        )
    }

    private fun assertPassEvaluationWithMessages(evaluation: Evaluation, matchedNames: String, vararg passSpecificMessages: String) {
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passSpecificMessages).containsOnly(*passSpecificMessages)
        assertThat(evaluation.passGeneralMessages).containsOnly("History of $matchedNames")
    }
}