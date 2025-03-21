package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.EvaluationTestFactory
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate
import org.junit.Test

class LabMeasurementEvaluatorTest {
   
    @Test
    fun `Should evaluate`() {
        val measurement = LabMeasurement.ALBUMIN
        val function = LabMeasurementEvaluator(measurement, passingLabEvaluationFunction, ALWAYS_VALID_DATE, ALWAYS_VALID_DATE)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
        val labValue: LabValue = LabTestFactory.create(measurement, date = TEST_DATE)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(LabTestFactory.withLabValue(labValue)))
    }

    @Test
    fun `Should ignore old dates and invalid units`() {
        val measurement = LabMeasurement.ALBUMIN
        val function = LabMeasurementEvaluator(measurement, passingLabEvaluationFunction, TEST_DATE, TEST_DATE)
        val wrongUnit: LabValue = LabTestFactory.create(value = 0.0, date = TEST_DATE).copy(measurement = measurement)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(LabTestFactory.withLabValue(wrongUnit)))
        val oldDate: LabValue = LabTestFactory.create(measurement, date = TEST_DATE.minusDays(1))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(LabTestFactory.withLabValue(oldDate)))
    }

    @Test
    fun `Should evaluate to recoverable pass in case measurement is older than pass date`() {
        val measurement = LabMeasurement.ALBUMIN
        val function = LabMeasurementEvaluator(measurement, passingLabEvaluationFunction, TEST_DATE.minusDays(20), TEST_DATE.plusDays(20))
        val warnDate: LabValue = LabTestFactory.create(measurement, date = TEST_DATE)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(LabTestFactory.withLabValue(warnDate)))
    }

    companion object {
        private val TEST_DATE = LocalDate.of(2020, 4, 20)
        private val ALWAYS_VALID_DATE = TEST_DATE.minusDays(2)

        private val passingLabEvaluationFunction: LabEvaluationFunction = object : LabEvaluationFunction {
            override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
                return EvaluationTestFactory.withResult(EvaluationResult.PASS)
            }
        }
    }
}