package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
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

class HasHadPriorConditionComplicationOrToxicityWithIcdCodeTest {
    private val icdModel = TestIcdFactory.createModelWithSpecificNodes(listOf("child", "otherTarget", "childParent", "extension"))
    private val referenceDate = LocalDate.of(2024, 12, 6)
    private val function = HasHadPriorConditionComplicationOrToxicityWithIcdCode(
        icdModel, setOf(IcdCode(parentCode)), diseaseDescription, referenceDate
    )
    private val minimalPatient = TestPatientFactory.createMinimalTestWGSPatientRecord()

    private val complicationWithTargetCode = OtherConditionTestFactory.complication(icdMainCode = parentCode, name = COMPLICATION_NAME)
    private val complicationWithChildOfTargetCode = complicationWithTargetCode.copy(icdCodes = setOf(IcdCode(childCode)))

    private val conditionWithTargetCode = OtherConditionTestFactory.priorOtherCondition(
        icdMainCode = parentCode,
        name = OTHER_CONDITION_NAME,
        isContraindication = true
    )
    private val conditionWithChildOfTargetCode = conditionWithTargetCode.copy(icdCodes = setOf(IcdCode(childCode)))

    @Test
    fun `Should fail when no matching ICD code in prior other conditions, complications or toxicities`() {
        val evaluation = function.evaluate(minimalPatient)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessages)
            .containsOnly("Has no other condition belonging to category $diseaseDescription")
    }

    @Test
    fun `Should pass when ICD code or parent code of other condition matches code of target title`() {
        listOf(conditionWithTargetCode, conditionWithChildOfTargetCode).forEach {
            assertPassEvaluationWithMessages(
                function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(it)),
                "History of other condition"
            )
        }
    }

    @Test
    fun `Should pass when ICD code or parent code of complication matches code of target title`() {
        listOf(complicationWithChildOfTargetCode, complicationWithTargetCode).forEach {
            assertPassEvaluationWithMessages(
                function.evaluate(OtherConditionTestFactory.withComplications(listOf(it))),
                "History of complication"
            )
        }
    }

    @Test
    fun `Should pass when ICD code or parent code of toxicity from questionnaire matches code of target title`() {
        listOf(childCode, parentCode).forEach {
            assertPassEvaluationWithMessages(
                function.evaluate(OtherConditionTestFactory.withToxicities(listOf(toxicity(ToxicitySource.QUESTIONNAIRE, IcdCode(it), 1)))),
                "History of toxicity"
            )
        }
    }

    @Test
    fun `Should pass when ICD code or parent code of toxicity from EHR with at least grade 2 matches code of target title`() {
        listOf(childCode, parentCode).forEach {
            assertPassEvaluationWithMessages(
                function.evaluate(OtherConditionTestFactory.withToxicities(listOf(toxicity(ToxicitySource.EHR, IcdCode(it), 2)))),
                "History of toxicity"
            )
        }
    }

    @Test
    fun `Should evaluate to undetermined when ICD main code matches but extension code is unknown`() {
        val function = HasHadPriorConditionComplicationOrToxicityWithIcdCode(
            icdModel, setOf(IcdCode(parentCode, "extensionCode")), diseaseDescription, referenceDate
        )

        assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.priorOtherCondition(icdMainCode = parentCode, icdExtensionCode = null)
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
                    OtherConditionTestFactory.withToxicities(
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
                    toxicities = listOf(toxicity, otherTox),
                    complications = listOf(complicationWithTargetCode),
                    priorOtherConditions = listOf(conditionWithTargetCode)
                )
            ),
            "History of complication, other condition, pneumonitis and toxicity"
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

    private fun assertPassEvaluationWithMessages(evaluation: Evaluation, vararg passMessages: String) {
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passMessages).containsOnly(*passMessages)
    }
}