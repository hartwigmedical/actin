package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import junit.framework.TestCase
import org.junit.Test
import java.time.LocalDate

class HasHadPriorConditionWithNameRecentlyTest {

    @Test
    fun canEvaluate() {
        val nameToFind = "severe condition"
        val minDate = LocalDate.of(2021, 8, 2)
        val function = HasHadPriorConditionWithNameRecently(nameToFind, minDate)

        // Fail when no prior conditions
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(emptyList())))

        // Fail when a matching prior condition was too long ago.
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(
                OtherConditionTestFactory.priorOtherCondition(name = "severe condition", year = 2020))
            )
        )

        // Fail when a recent prior condition does not match the name.
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(
                OtherConditionTestFactory.priorOtherCondition(name = "benign condition", year = 2022))
            )
        )

        // Can not correctly determine if the date is before or after
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(
                OtherConditionTestFactory.priorOtherCondition(name = "severe condition", year = 2021))
            )
        )

        // Pass when matching condition is after the minDate.
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(
                OtherConditionTestFactory.priorOtherCondition(name = "severe condition", year = 2022))
            )
        )

        // Warn when matching condition is just after the minDate.
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN, function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.priorOtherCondition(name = "severe condition", year = 2021, month = 9))
            )
        )
    }
}