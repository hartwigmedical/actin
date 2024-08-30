package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test
import java.time.LocalDate

class HasHadPriorConditionWithDoidsFromSetRecentlyTest {

    private val minDate: LocalDate = LocalDate.of(2021, 8, 2)
    private val doidsToFind = DoidConstants.THROMBOEMBOLIC_EVENT_DOID_SET
    private val function = HasHadPriorConditionWithDoidsFromSetRecently(
        TestDoidModelFactory.createMinimalTestDoidModel(),
        doidsToFind,
        "thrombo-embolic event",
        minDate
    )

    @Test
    fun `Should warn if condition in history with correct DOID term and within first 2 months of specified time-frame`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.priorOtherCondition(
                        doids = doidsToFind, year = minDate.plusMonths(1).year, month = minDate.plusMonths(1).monthValue
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass if condition in history with correct DOID term and within specified time-frame but not in first 2 months`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.priorOtherCondition(
                        doids = doidsToFind, year = minDate.plusYears(1).year, month = 1
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass if both pass and warn conditions are met - two conditions with correct DOID in time-frame of which one in first 2 months`() {
        val conditions = OtherConditionTestFactory.withPriorOtherConditions(
            listOf(
                OtherConditionTestFactory.priorOtherCondition(
                    doids = doidsToFind, year = minDate.plusYears(1).year, month = 1
                ),
                OtherConditionTestFactory.priorOtherCondition(
                    doids = doidsToFind, year = minDate.plusMonths(1).year, month = minDate.plusMonths(1).monthValue
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(conditions))
    }

    @Test
    fun `Should evaluate to undetermined if condition in history with correct DOID term but unknown date`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.priorOtherCondition(
                        doids = doidsToFind, year = null
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail if no conditions with correct DOID term in history`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.priorOtherCondition(
                        doids = setOf(DoidConstants.DIABETES_DOID), year = 2023
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
    fun `Should fail when other condition with correct DOID present in history, but outside of evaluated timeframe`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.priorOtherCondition(
                        doids = doidsToFind, year = minDate.minusYears(1).year, month = 1
                    )
                )
            )
        )
    }

}