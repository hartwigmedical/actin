package com.hartwig.actin.algo.evaluation.othercondition;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.junit.Test;

public class HasHadPriorConditionWithDoidRecentlyTest {

    @Test
    public void canEvaluate() {
        LocalDate minDate = LocalDate.of(2021, 8, 2);
        String matchDoid = "123";
        HasHadPriorConditionWithDoidRecently function =
                new HasHadPriorConditionWithDoidRecently(TestDoidModelFactory.createMinimalTestDoidModel(), matchDoid, minDate);

        // Fail when no prior conditions
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(Lists.newArrayList())));

        // Fail when a matching prior condition was too long ago.
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(OtherConditionTestFactory.builder()
                        .addDoids(matchDoid)
                        .year(2020)
                        .build())));

        // Fail when a recent prior condition does not have a matching doid
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(OtherConditionTestFactory.builder()
                        .addDoids("wrong doid")
                        .year(2022)
                        .build())));

        // Can't determine in case a matching prior condition has a date that could be before or after minDate.
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(OtherConditionTestFactory.builder()
                        .addDoids(matchDoid)
                        .year(2021)
                        .build())));

        // Pass when matching condition is after the minDate.
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(OtherConditionTestFactory.builder()
                        .addDoids(matchDoid)
                        .year(2022)
                        .build())));

        // Warn when matching condition is just after the minDate.
        assertEvaluation(EvaluationResult.WARN,
                function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(OtherConditionTestFactory.builder()
                        .addDoids(matchDoid)
                        .year(2021)
                        .month(9)
                        .build())));
    }
}