package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabValue
import org.junit.Test
import java.time.LocalDate

class HasAbnormalElectrolyteLevelsTest {
    private val refDate = LocalDate.of(2024, 7, 30)
    private val minValidDate = refDate.minusDays(90)
    private val minPassDate = refDate.minusDays(30)
    private val function = HasAbnormalElectrolyteLevels(minValidDate, minPassDate)
    private val labMeasurements = listOf(
        LabMeasurement.CALCIUM,
        LabMeasurement.PHOSPHATE,
        LabMeasurement.SODIUM,
        LabMeasurement.MAGNESIUM,
        LabMeasurement.POTASSIUM
    )
    private val labValuesWithinRef = labMeasurements.map { createLabValueWithinRef(it) }

    @Test
    fun `Should pass if one or multiple electrolyte lab values are above ULN`() {
        labMeasurements.forEach { evaluateLabValues(EvaluationResult.PASS, codesOfMeasurementsAboveRef = setOf(it.code)) }
        evaluateLabValues(EvaluationResult.PASS, codesOfMeasurementsAboveRef = labMeasurements.map { it.code }.toSet())
    }

    @Test
    fun `Should pass if one or multiple electrolyte lab values are under LLN`() {
        labMeasurements.forEach { evaluateLabValues(EvaluationResult.PASS, codesOfMeasurementsBelowRef = setOf(it.code)) }
        evaluateLabValues(EvaluationResult.PASS, codesOfMeasurementsBelowRef = labMeasurements.map { it.code }.toSet())
    }

    @Test
    fun `Should fail if all electrolyte lab values are within reference range`() {
        evaluateLabValues(EvaluationResult.FAIL, emptySet(), emptySet())
    }

    private fun createLabValueWithinRef(measurement: LabMeasurement): LabValue {
        return LabTestFactory.create(measurement, value = 80.0, refDate, refLimitUp = 100.0, refLimitLow = 50.0)
    }

    private fun evaluateLabValues(
        expected: EvaluationResult,
        codesOfMeasurementsAboveRef: Set<String> = emptySet(),
        codesOfMeasurementsBelowRef: Set<String> = emptySet()
    ) {
        val labValues = labValuesWithinRef.map { labValue ->
            when (labValue.code) {
                in codesOfMeasurementsAboveRef -> labValue.copy(value = 180.0, isOutsideRef = true)
                in codesOfMeasurementsBelowRef -> labValue.copy(value = 5.0, isOutsideRef = true)
                else -> labValue
            }
        }
        EvaluationAssert.assertEvaluation(expected, function.evaluate(LabTestFactory.withLabValues(labValues)))
    }
}