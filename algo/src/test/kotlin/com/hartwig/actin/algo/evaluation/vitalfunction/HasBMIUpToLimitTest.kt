package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight
import org.junit.Assert
import org.junit.Test
import java.time.LocalDateTime

class HasBMIUpToLimitTest {
    private val function: HasBMIUpToLimit = HasBMIUpToLimit(40)
    private val now = LocalDateTime.now()
    private val lastYear = now.minusYears(1)

    @Test
    fun `Should be undetermined when no body weights provided`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(VitalFunctionTestFactory.withBodyWeights(emptyList()))
        )
    }

    @Test
    fun `Should be undetermined when no body weights provided with expected unit`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                VitalFunctionTestFactory.withBodyWeights(
                    listOf(
                        ImmutableBodyWeight.builder()
                            .date(now)
                            .value(70.0)
                            .unit("pound")
                            .build()
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass if latest weight is less than warn threshold`() {
        val evaluation = function.evaluate(
            VitalFunctionTestFactory.withBodyWeights(
                listOf(
                    ImmutableBodyWeight.builder()
                        .date(now)
                        .value(70.57)
                        .unit("Kilogram")
                        .build(), ImmutableBodyWeight.builder().date(lastYear).value(100.0).unit("Kilogram").build()
                )
            )
        )
        assertEvaluation(EvaluationResult.PASS, evaluation)
        Assert.assertTrue(
            evaluation.passSpecificMessages()
                .contains("Patient weight 70.6 kg will not exceed BMI limit of 40 for height >= 1.33 m")
        )
    }

    @Test
    fun `Should fail if latest weight is greater than fail threshold`() {
        val evaluation = function.evaluate(
            VitalFunctionTestFactory.withBodyWeights(
                listOf(
                    ImmutableBodyWeight.builder()
                        .date(now)
                        .value(180.32)
                        .unit("Kilogram")
                        .build(), ImmutableBodyWeight.builder().date(lastYear).value(100.0).unit("Kilogram").build()
                )
            )
        )
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        Assert.assertTrue(
            evaluation.failSpecificMessages().contains("Patient weight 180.3 kg will exceed BMI limit of 40 for height < 2.12 m")
        )
    }

    @Test
    fun `Should warn if latest weight is greater than warn threshold`() {
        val evaluation = function.evaluate(
            VitalFunctionTestFactory.withBodyWeights(
                listOf(
                    ImmutableBodyWeight.builder()
                        .date(now)
                        .value(100.99)
                        .unit("Kilogram")
                        .build(), ImmutableBodyWeight.builder().date(lastYear).value(80.0).unit("Kilogram").build()
                )
            )
        )
        assertEvaluation(EvaluationResult.WARN, evaluation)
        Assert.assertTrue(
            evaluation.warnSpecificMessages().contains("Patient weight 101.0 kg will exceed BMI limit of 40 for height < 1.59 m")
        )
    }
}