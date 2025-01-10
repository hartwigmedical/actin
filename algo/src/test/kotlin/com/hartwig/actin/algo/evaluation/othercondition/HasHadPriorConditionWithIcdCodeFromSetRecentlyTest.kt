package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.datamodel.IcdNode
import java.time.LocalDate
import org.junit.Test

class HasHadPriorConditionWithIcdCodeFromSetRecentlyTest {

    private val minDate: LocalDate = LocalDate.of(2021, 8, 2)
    private val targetIcdCodes = IcdConstants.STROKE_SET.map { IcdCode(it) }.toSet()
    private val icdModel = IcdModel.create(targetIcdCodes.map { IcdNode(it.mainCode, emptyList(), it.mainCode + "node") })
    private val function =
        HasHadPriorConditionWithIcdCodeFromSetRecently(icdModel, targetIcdCodes, "stroke", minDate)

    @Test
    fun `Should warn if condition in history with correct ICD code and within first 2 months of specified time-frame`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.priorOtherCondition(
                       icdMainCode = targetIcdCodes.first().mainCode, year = minDate.plusMonths(1).year, month = minDate.plusMonths(1).monthValue
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass if condition in history with correct ICD code and within specified time-frame but not in first 2 months`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.priorOtherCondition(
                        icdMainCode = targetIcdCodes.first().mainCode, year = minDate.plusYears(1).year, month = 1
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass if both pass and warn conditions are met - two conditions with correct ICD code in time-frame of which one in first 2 months`() {
        val conditions = OtherConditionTestFactory.withPriorOtherConditions(
            listOf(
                OtherConditionTestFactory.priorOtherCondition(
                    icdMainCode = targetIcdCodes.first().mainCode, year = minDate.plusYears(1).year, month = 1
                ),
                OtherConditionTestFactory.priorOtherCondition(
                    icdMainCode = targetIcdCodes.first().mainCode, year = minDate.plusMonths(1).year, month = minDate.plusMonths(1).monthValue
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(conditions))
    }

    @Test
    fun `Should evaluate to undetermined if condition in history with correct ICD code but unknown date`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.priorOtherCondition(
                        icdMainCode = targetIcdCodes.first().mainCode, year = null
                    )
                )
            )
        )
    }

    @Test
    fun `Should evaluate to undetermined if condition matches main ICD code but has unknown extension`() {
        val function = HasHadPriorConditionWithIcdCodeFromSetRecently(
            icdModel, setOf(IcdCode(IcdConstants.STROKE_NOS_CODE, "extensionCode")), "stroke", minDate
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.priorOtherCondition(
                        icdMainCode = IcdConstants.STROKE_NOS_CODE, icdExtensionCode = null
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail if no conditions with correct ICD code in history`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.priorOtherCondition(
                        icdMainCode = IcdConstants.HYPOMAGNESEMIA_CODE, year = 2023
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail when no conditions present in history`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherConditions(emptyList())
            )
        )
    }

    @Test
    fun `Should fail when other condition with correct ICD code present in history, but outside of evaluated timeframe`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.priorOtherCondition(
                        icdMainCode = targetIcdCodes.first().mainCode, year = minDate.minusYears(1).year, month = 1
                    )
                )
            )
        )
    }

}