package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.laboratory.LabTestFactory
import com.hartwig.actin.clinical.interpretation.LabMeasurement
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
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(LabTestFactory.withLabValue(calciumValue.copy(value = refLimitUp.times(2))))
        )
    }

    @Test
    fun `Should warn if ionized calcium is above ULN`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(LabTestFactory.withLabValue(ionizedCalciumValue.copy(value = refLimitUp.times(2))))
        )
    }

    @Test
    fun `Should warn if corrected calcium is above ULN`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(LabTestFactory.withLabValue(correctedCalciumValue.copy(value = refLimitUp.times(2))))
        )
    }

    @Test
    fun `Should evaluate to undetermined if calcium cannot be determined`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(LabTestFactory.withLabValue(calciumValue.copy(refLimitUp = null)))
        )
    }

    @Test
    fun `Should evaluate to undetermined if ionized calcium cannot be determined`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(LabTestFactory.withLabValue(ionizedCalciumValue.copy(refLimitUp = null)))
        )
    }

    @Test
    fun `Should evaluate to undetermined if corrected calcium cannot be determined`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(LabTestFactory.withLabValue(correctedCalciumValue.copy(refLimitUp = null)))
        )
    }

    @Test
    fun `Should fail all values under ULN`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(LabTestFactory.withLabValues(listOf(calciumValue, correctedCalciumValue, ionizedCalciumValue)))
        )
    }
}