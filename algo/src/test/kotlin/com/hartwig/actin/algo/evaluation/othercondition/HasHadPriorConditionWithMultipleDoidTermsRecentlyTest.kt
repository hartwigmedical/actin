package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test
import java.time.LocalDate

class HasHadPriorConditionWithMultipleDoidTermsRecentlyTest {

    private val minDate: LocalDate = LocalDate.of(2021, 8, 2)
    private val doidsToFind = DoidConstants.THROMBO_EMBOLIC_EVENT_DOID_SET
    private val function = HasHadPriorConditionWithMultipleDoidTermsRecently(
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
                        doids = doidsToFind, year = 2021, month = 9
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
                        doids = doidsToFind, year = 2022, month = 1
                    )
                )
            )
        )
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


}