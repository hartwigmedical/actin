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
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val OTHER_CONDITION_NAME: String = "other condition"
private const val COMPLICATION_NAME: String = "complication"
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
    private val complicationWithTargetCode = ComorbidityTestFactory.complication(icdMainCode = parentCode, name = COMPLICATION_NAME)
    private val complicationWithChildOfTargetCode = complicationWithTargetCode.copy(icdCodes = setOf(IcdCode(childCode)))
    private val conditionWithTargetCode = ComorbidityTestFactory.otherCondition(
        name = OTHER_CONDITION_NAME,
        icdMainCode = parentCode
    )
    private val conditionWithChildOfTargetCode = conditionWithTargetCode.copy(icdCodes = setOf(IcdCode(childCode)))
    private val function =
        HasHadComorbidityWithIcdCode(
            icdModel,
            targetIcdCodes + setOf(IcdCode(parentCode)),
            diseaseDescription,
            referenceDate
        )


    @Test
    fun `Should pass if condition with correct ICD code in history`() {
        val conditions =
            ComorbidityTestFactory.otherCondition("pneumonitis", icdMainCode = IcdConstants.PNEUMONITIS_DUE_TO_EXTERNAL_AGENTS_BLOCK)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComorbidityTestFactory.withOtherCondition(conditions)))
    }

    @Test
    fun `Should evaluate to undetermined for condition with unknown extension`() {
        val function = HasHadComorbidityWithIcdCode(
            TestIcdFactory.createTestModel(),
            setOf(IcdCode(IcdConstants.PNEUMONITIS_DUE_TO_EXTERNAL_AGENTS_BLOCK, "extensionCode")),
            "respiratory compromise",
            referenceDate
        )
        val conditions = ComorbidityTestFactory.otherCondition(
            "pneumonitis",
            icdMainCode = IcdConstants.PNEUMONITIS_DUE_TO_EXTERNAL_AGENTS_BLOCK,
            icdExtensionCode = null
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ComorbidityTestFactory.withOtherCondition(conditions)))
    }

    @Test
    fun `Should fail if no conditions with correct ICD code in history`() {
        val conditions = ComorbidityTestFactory.otherCondition("stroke", icdMainCode = IcdConstants.CEREBRAL_ISCHAEMIA_BLOCK)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withOtherCondition(conditions)))
    }

    @Test
    fun `Should fail if no conditions present in history`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withOtherConditions(emptyList())))
    }

    @Test
    fun `Should fail when no matching ICD code in other conditions, complications or toxicities`() {
        val evaluation = function.evaluate(minimalPatient)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessagesStrings())
            .containsOnly("Has no other condition belonging to category $diseaseDescription")
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
    fun `Should pass when ICD code or parent code of complication matches code of target title`() {
        listOf(complicationWithChildOfTargetCode, complicationWithTargetCode).forEach {
            assertPassEvaluationWithMessages(
                function.evaluate(ComorbidityTestFactory.withComplications(listOf(it))),
                "complication"
            )
        }
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
    fun `Should evaluate to undetermined when ICD main code matches but extension code is unknown`() {
        val function = HasHadComorbidityWithIcdCode(
            icdModel,
            setOf(IcdCode(parentCode, "extensionCode")),
            diseaseDescription,
            referenceDate
        )

        assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(
                ComorbidityTestFactory.withOtherCondition(
                    ComorbidityTestFactory.otherCondition(icdMainCode = parentCode, icdExtensionCode = null)
                )
            )
        )
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
    fun `Should combine multiple conditions, toxicities and complications into one message`() {
        val toxicity = toxicity(ToxicitySource.QUESTIONNAIRE, IcdCode(childCode), 2)
        val otherTox = toxicity.copy(name = "pneumonitis")
        assertPassEvaluationWithMessages(
            function.evaluate(
                minimalPatient.copy(
                    comorbidities = listOf(toxicity, otherTox, complicationWithTargetCode, conditionWithTargetCode)
                )
            ),
            "complication, other condition, pneumonitis and toxicity"
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