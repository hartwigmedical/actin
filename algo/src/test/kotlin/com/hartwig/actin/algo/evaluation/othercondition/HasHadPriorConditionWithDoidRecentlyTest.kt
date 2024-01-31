package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.priorOtherCondition
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.withPriorOtherCondition
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.withPriorOtherConditions
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test
import java.time.LocalDate

class HasHadPriorConditionWithDoidRecentlyTest {

    @Test
    fun canEvaluate() {
        val minDate = LocalDate.of(2021, 8, 2)
        val matchDoid = "123"
        val function = HasHadPriorConditionWithDoidRecently(TestDoidModelFactory.createMinimalTestDoidModel(), matchDoid, minDate)

        // Fail when no prior conditions
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withPriorOtherConditions(emptyList())))

        // Fail when a matching prior condition was too long ago.
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(withPriorOtherCondition(priorOtherCondition(doids = setOf(matchDoid), year = 2020)))
        )

        // Fail when a recent prior condition does not have a matching doid
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(withPriorOtherCondition(priorOtherCondition(doids = setOf("wrong doid"), year = 2022)))
        )

        // Can't determine in case a matching prior condition has a date that could be before or after minDate.
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withPriorOtherCondition(priorOtherCondition(doids = setOf(matchDoid), year = 2021)))
        )

        // Pass when matching condition is after the minDate.
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withPriorOtherCondition(priorOtherCondition(doids = setOf(matchDoid), year = 2022)))
        )

        // Warn when matching condition is just after the minDate.
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(withPriorOtherCondition(priorOtherCondition(doids = setOf(matchDoid), year = 2021, month = 9)))
        )
    }
}