package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.junit.Test
import java.time.LocalDate

class HasAbnormalElectrolyteLevelsTest {


    private val refDate = LocalDate.of(2024, 7, 30)
    private val minValidDate = refDate.minusDays(90)
    private val minPassDate = refDate.minusDays(30)
    private val function = HasAbnormalElectrolyteLevels(minValidDate, minPassDate)
    private val labMeasurements = listOf(
        LabMeasurement.CALCIUM,
        LabMeasurement.PHOSPHORUS,
        LabMeasurement.SODIUM,
        LabMeasurement.MAGNESIUM,
        LabMeasurement.POTASSIUM
    )
    private val labValuesWithinRef = labMeasurements.map { createLabValueWithinRef(it) }

    @Test
    fun `Should pass if one or multiple electrolyte lab values are above ULN`() {
        labMeasurements.map { evaluateLabvalues(EvaluationResult.PASS, measurementsAboveRef = listOf(it)) }
        evaluateLabvalues(EvaluationResult.PASS, measurementsAboveRef = labMeasurements)
    }

    @Test
    fun `Should pass if one or multiple electrolyte lab values are under LLN`() {
        labMeasurements.map { evaluateLabvalues(EvaluationResult.PASS, measurementsBelowRef = listOf(it)) }
        evaluateLabvalues(EvaluationResult.PASS, measurementsBelowRef = labMeasurements)
    }

    @Test
    fun `Should fail if all electrolyte lab values are within reference range`() {
        evaluateLabvalues(EvaluationResult.FAIL, emptyList(), emptyList())
    }

    private fun createLabValueWithinRef(measurement: LabMeasurement): LabValue {
        return LabTestFactory.create(measurement, value = 80.0, refDate, refLimitUp = 100.0, refLimitLow = 50.0)
    }

    private fun evaluateLabvalues(
        expected: EvaluationResult,
        measurementsAboveRef: List<LabMeasurement> = emptyList(),
        measurementsBelowRef: List<LabMeasurement> = emptyList()
    ) {
        val measurementsToFilterOut = (measurementsAboveRef + measurementsBelowRef).map { it.code }
        val valuesInsideRef = labValuesWithinRef.filterNot { it.code in measurementsToFilterOut }
        val valuesAboveRef = measurementsAboveRef.map {
            LabTestFactory.create(it, value = 180.0, refDate, refLimitUp = 100.0, refLimitLow = 50.0)
        }
        val valuesBelowRef = measurementsBelowRef.map {
            LabTestFactory.create(it, value = 5.0, refDate, refLimitUp = 100.0, refLimitLow = 50.0)
        }
        val record = LabTestFactory.withLabValues(valuesInsideRef + valuesAboveRef + valuesBelowRef)
        EvaluationAssert.assertEvaluation(expected, function.evaluate(record))
    }
}