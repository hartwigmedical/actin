package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import org.junit.Test
import java.time.LocalDate

class HasHadPriorConditionWithNameRecentlyTest {

    private val nameToFind = "severe condition"
    private val minDate = LocalDate.of(2021, 8, 2)
    private val function = HasHadPriorConditionWithNameRecently(nameToFind, minDate)

    @Test
    fun `Should fail when prior conditions is an empty list`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherConditions(
                    emptyList()
                )
            )
        )
    }

    @Test
    fun `Should fail when a matching prior condition was too long ago`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.priorOtherCondition(
                        name = "severe condition", year = 2020
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail when a recent prior condition does not match name`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.priorOtherCondition(
                        name = "benign condition", year = 2022
                    )
                )
            )
        )
    }

    @Test
    fun `Should evaluate to undetermined if the given date is in range`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.priorOtherCondition(
                        name = "severe condition", year = 2021
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass when matching condition is after the minDate`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.priorOtherCondition(
                        name = "severe condition", year = 2022
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass when two pass matching condition are given`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherConditions(
                    listOf(
                        OtherConditionTestFactory.priorOtherCondition(
                            name = "severe condition", year = 2022
                        ),
                        OtherConditionTestFactory.priorOtherCondition(
                            name = "severe condition", year = 2023
                        )
                    )
                )
            )
        )
    }


    @Test
    fun `Should warn when matching condition is just after the minDate`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.priorOtherCondition(
                        name = "severe condition", year = 2021, month = 9
                    )
                )
            )
        )
    }
}