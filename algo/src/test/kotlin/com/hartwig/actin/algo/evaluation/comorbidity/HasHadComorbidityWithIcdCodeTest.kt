package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.TestIcdFactory
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val OTHER_CONDITION_NAME: String = "other condition"
private const val TOXICITY_NAME: String = "toxicity"
private const val childCode = "childCode"
private const val parentCode = "childParentCode"
private const val diseaseDescription = "parent disease"

class HasHadComorbidityWithIcdCodeTest {
    private val targetIcdCodes = IcdConstants.RESPIRATORY_COMPROMISE_SET.map { IcdCode(it) }.toSet()
    private val icdModel =
        TestIcdFactory.createModelWithSpecificNodes(listOf("child", "otherTarget", "childParent", "extension", parentCode))
    private val referenceDate = LocalDate.of(2024, 12, 6)
    private val minimalPatient = TestPatientFactory.createMinimalTestWGSPatientRecord()
    private val conditionWithTargetCode = ComorbidityTestFactory.otherCondition(name = OTHER_CONDITION_NAME, icdMainCode = parentCode)
    private val conditionWithChildOfTargetCode = conditionWithTargetCode.copy(icdCodes = setOf(IcdCode(childCode)))
    private val function = HasHadComorbidityWithIcdCode(
        icdModel,
        targetIcdCodes + setOf(IcdCode(parentCode)),
        diseaseDescription,
        referenceDate
    )

    @Test
    fun `Should pass if comorbidity with correct ICD code in history`() {
        val comorbidities =
            ComorbidityTestFactory.otherCondition("pneumonitis", icdMainCode = IcdConstants.PNEUMONITIS_DUE_TO_EXTERNAL_AGENTS_BLOCK)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComorbidityTestFactory.withOtherCondition(comorbidities)))
    }

    @Test
    fun `Should evaluate to undetermined for comorbidity with unknown extension`() {
        val function = HasHadComorbidityWithIcdCode(
            TestIcdFactory.createTestModel(),
            setOf(IcdCode(IcdConstants.PNEUMONITIS_DUE_TO_EXTERNAL_AGENTS_BLOCK, "extensionCode")),
            "respiratory compromise",
            referenceDate
        )
        val comorbidities = ComorbidityTestFactory.otherCondition(
            "pneumonitis",
            icdMainCode = IcdConstants.PNEUMONITIS_DUE_TO_EXTERNAL_AGENTS_BLOCK,
            icdExtensionCode = null
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ComorbidityTestFactory.withOtherCondition(comorbidities)))
    }

    @Test
    fun `Should fail when patient has no comorbidities`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withOtherConditions(emptyList())))
    }

    @Test
    fun `Should fail if no conditions with correct ICD code in history`() {
        val conditions = ComorbidityTestFactory.otherCondition("stroke", icdMainCode = IcdConstants.CEREBRAL_ISCHAEMIA_BLOCK)
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(ComorbidityTestFactory.withOtherCondition(conditions))
        )
    }

    @Test
    fun `Should pass when ICD code or parent code of other condition matches code of target title`() {
        listOf(conditionWithTargetCode, conditionWithChildOfTargetCode).forEach {
            assertPassEvaluationWithMessages(
                function.evaluate(ComorbidityTestFactory.withOtherCondition(it)),
                "other condition"
            )
        }
    }

    @Test
    fun `Should pass when ICD code or parent code of intolerance matches code of target title`() {
        val intoleranceWithTargetCode = ComorbidityTestFactory.intolerance(icdMainCode = parentCode, name = "intolerance")
        val intoleranceWithChildOfTargetCode = intoleranceWithTargetCode.copy(icdCodes = setOf(IcdCode(childCode)))
        listOf(intoleranceWithChildOfTargetCode, intoleranceWithTargetCode).forEach {
            val evaluation = function.evaluate(ComorbidityTestFactory.withIntolerances(listOf(it)))
            assertEvaluation(EvaluationResult.PASS, evaluation)
            assertThat(evaluation.passMessagesStrings()).containsOnly("Has intolerance to intolerance")
        }
    }

    @Test
    fun `Should pass when ICD code or parent code of intolerance and other condition matches target code`() {
        val intoleranceWithTargetCode = ComorbidityTestFactory.intolerance(icdMainCode = parentCode, name = "intolerance")
        val otherConditionWithTargetCode = ComorbidityTestFactory.otherCondition(icdMainCode = parentCode, name = "other condition")
        val evaluation =
            function.evaluate(ComorbidityTestFactory.withComorbidities(listOf(intoleranceWithTargetCode, otherConditionWithTargetCode)))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passMessagesStrings()).containsAll(setOf("Has intolerance to intolerance", "Has history of other condition"))
    }

    @Test
    fun `Should pass when ICD code or parent code of toxicity from questionnaire matches code of target title`() {
        listOf(childCode, parentCode).forEach {
            assertPassEvaluationWithMessages(
                function.evaluate(ComorbidityTestFactory.withToxicities(listOf(toxicity(ToxicitySource.QUESTIONNAIRE, IcdCode(it), 1)))),
                "toxicity"
            )
        }
    }

    @Test
    fun `Should pass when ICD code or parent code of toxicity from EHR with at least grade 2 matches code of target title`() {
        listOf(childCode, parentCode).forEach {
            assertPassEvaluationWithMessages(
                function.evaluate(ComorbidityTestFactory.withToxicities(listOf(toxicity(ToxicitySource.EHR, IcdCode(it), 2)))),
                "toxicity"
            )
        }
    }

    @Test
    fun `Should fail when ICD code or parent code of toxicity from EHR with grade less than 2 matches code of target title`() {
        listOf(childCode, parentCode).forEach {
            assertEvaluation(
                EvaluationResult.FAIL,
                function.evaluate(
                    ComorbidityTestFactory.withToxicities(
                        listOf(toxicity(ToxicitySource.EHR, IcdCode(it), 1))
                    )
                )
            )
        }
    }

    @Test
    fun `Should combine multiple toxicities and other conditions in one message`() {
        val toxicity = toxicity(ToxicitySource.QUESTIONNAIRE, IcdCode(childCode), 2)
        val otherTox = toxicity.copy(name = "pneumonitis")
        assertPassEvaluationWithMessages(
            function.evaluate(
                minimalPatient.copy(
                    comorbidities = listOf(toxicity, otherTox, conditionWithTargetCode, conditionWithChildOfTargetCode)
                )
            ),
            "other condition, pneumonitis and toxicity"
        )
    }

    private fun toxicity(toxicitySource: ToxicitySource, icdCode: IcdCode, grade: Int?): Toxicity {
        return Toxicity(
            icdCodes = setOf(icdCode),
            name = TOXICITY_NAME,
            evaluatedDate = referenceDate,
            source = toxicitySource,
            grade = grade
        )
    }

    private fun assertPassEvaluationWithMessages(evaluation: Evaluation, matchedNames: String) {
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passMessagesStrings()).containsOnly("Has history of $matchedNames")
    }
}
