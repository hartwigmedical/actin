package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionTestFactory.height
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionTestFactory.weight
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class HasBMIUpToLimitTest {
    private val function: HasBMIUpToLimit = HasBMIUpToLimit(40, LocalDate.of(2023, 12, 1))
    private val referenceDate = LocalDateTime.of(2023, 12, 2, 0, 0)

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
                    listOf(weight(date = referenceDate, value = 70.0, unit = "pound"))
                )
            )
        )
    }

    @Test
    fun `Should pass if body height provided and calculated BMI below max BMI`() {
        val weight = weight(date = referenceDate, value = 70.0, unit = "Kilogram")
        val height = height(date = referenceDate, value = 180.0, unit = "centimeters", valid = true)
        val evaluation = function.evaluate(VitalFunctionTestFactory.withBodyWeightsAndHeight(listOf(weight), height))
        assertEvaluation(EvaluationResult.PASS, evaluation)
    }

    @Test
    fun `Should fail if body height provided and calculated BMI above max BMI`() {
        val weight = weight(date = referenceDate, value = 140.0, unit = "Kilogram")
        val height = height(date = referenceDate, value = 160.0, unit = "centimeters", valid = true)
        val evaluation = function.evaluate(VitalFunctionTestFactory.withBodyWeightsAndHeight(listOf(weight), height))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
    }

    @Test
    fun `Should pass if median weight is less than warn threshold`() {
        val evaluation = function.evaluate(
            VitalFunctionTestFactory.withBodyWeights(
                listOf(
                    weight(date = referenceDate, value = 70.0, unit = "Kilogram"),
                    weight(date = referenceDate.plusDays(1), value = 80.0, unit = "Kilogram")
                )
            )
        )
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passSpecificMessages).contains("Median weight 75.0 kg will not exceed BMI limit of 40 for height >= 1.37 m")
    }

    @Test
    fun `Should pass if median weight is in kilograms instead of kilogram and value is less than warn threshold`() {
        val evaluation = function.evaluate(
            VitalFunctionTestFactory.withBodyWeights(
                listOf(
                    weight(date = referenceDate, value = 70.0, unit = "Kilograms"),
                    weight(date = referenceDate.plusDays(1), value = 80.0, unit = "Kilograms")
                )
            )
        )
        assertEvaluation(EvaluationResult.PASS, evaluation)
    }

    @Test
    fun `Should fail if median weight is greater than fail threshold`() {
        val evaluation = function.evaluate(
            VitalFunctionTestFactory.withBodyWeights(
                listOf(
                    weight(date = referenceDate, value = 180.0, unit = "Kilogram"),
                    weight(date = referenceDate.plusDays(1), value = 170.0, unit = "Kilogram")
                )
            )
        )
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failSpecificMessages).contains("Median weight 175.0 kg will exceed BMI limit of 40 for height < 2.09 m")
    }

    @Test
    fun `Should warn if latest weight is greater than warn threshold and less than fail threshold`() {
        val evaluation = function.evaluate(
            VitalFunctionTestFactory.withBodyWeights(
                listOf(
                    weight(date = referenceDate, value = 105.0, unit = "Kilogram"),
                    weight(date = referenceDate.plusDays(1), value = 100.0, unit = "Kilogram")
                )
            )
        )
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnSpecificMessages).contains("Median weight 102.5 kg will exceed BMI limit of 40 for height < 1.60 m")
    }
}