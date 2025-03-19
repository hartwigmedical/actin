package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.laboratory.LabTestFactory
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test
import java.time.LocalDate

class HasPotentialSymptomaticHypercalcemiaTest {

    private val referenceDate = LocalDate.of(2024, 7, 9)
    private val minValidDate = referenceDate.minusDays(90)
    private val refLimitUp = 100.0
    private val calciumValue = LabTestFactory.create(LabMeasurement.CALCIUM, date = referenceDate, refLimitUp = refLimitUp)
    private val ionizedCalciumValue = LabTestFactory.create(LabMeasurement.IONIZED_CALCIUM, date = referenceDate, refLimitUp = refLimitUp)
    private val correctedCalciumValue =
        LabTestFactory.create(LabMeasurement.CORRECTED_CALCIUM, date = referenceDate, refLimitUp = refLimitUp)
    private val function = HasPotentialSymptomaticHypercalcemia(minValidDate)

    @Test
    fun `Should warn if calcium is above ULN`() {
        val labValues = listOf(
            calciumValue.copy(value = refLimitUp.times(2)),
            ionizedCalciumValue.copy(value = refLimitUp),
            correctedCalciumValue.copy(value = refLimitUp)
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(LabTestFactory.withLabValues(labValues))
        )
    }

    @Test
    fun `Should warn if ionized calcium is above ULN`() {
        val labValues = listOf(
            calciumValue.copy(value = refLimitUp),
            ionizedCalciumValue.copy(value = refLimitUp.times(2)),
            correctedCalciumValue.copy(value = refLimitUp)
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(LabTestFactory.withLabValues(labValues))
        )
    }

    @Test
    fun `Should warn if corrected calcium is above ULN`() {
        val labValues = listOf(
            calciumValue.copy(value = refLimitUp),
            ionizedCalciumValue.copy(value = refLimitUp),
            correctedCalciumValue.copy(value = refLimitUp.times(2))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(LabTestFactory.withLabValues(labValues))
        )
    }

    @Test
    fun `Should evaluate to undetermined if calcium cannot be determined`() {
        val labValues = listOf(
            calciumValue.copy(value = refLimitUp, refLimitUp = null),
            ionizedCalciumValue.copy(value = refLimitUp),
            correctedCalciumValue.copy(value = refLimitUp)
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(LabTestFactory.withLabValues(labValues))
        )
    }

    @Test
    fun `Should evaluate to undetermined if ionized calcium cannot be determined`() {
        val labValues = listOf(
            calciumValue.copy(value = refLimitUp),
            ionizedCalciumValue.copy(value = refLimitUp, refLimitUp = null),
            correctedCalciumValue.copy(value = refLimitUp)
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(LabTestFactory.withLabValues(labValues))
        )
    }

    @Test
    fun `Should evaluate to undetermined if corrected calcium cannot be determined`() {
        val labValues = listOf(
            calciumValue.copy(value = refLimitUp),
            ionizedCalciumValue.copy(value = refLimitUp),
            correctedCalciumValue.copy(value = refLimitUp, refLimitUp = null)
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(LabTestFactory.withLabValues(labValues))
        )
    }

    @Test
    fun `Should evaluate to undetermined if calcium lab values not present`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(LabTestFactory.withLabValue(LabTestFactory.create(LabMeasurement.ALBUMIN, date = referenceDate)))
        )
    }

    @Test
    fun `Should fail if all values under ULN`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(LabTestFactory.withLabValues(listOf(calciumValue, correctedCalciumValue, ionizedCalciumValue)))
        )
    }
}