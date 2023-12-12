package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.EvaluationTestFactory
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.junit.Test
import java.time.LocalDate

class LabMeasurementEvaluatorTest {
    @Test
    fun canEvaluate() {
        val measurement = LabMeasurement.ALBUMIN
        val function = LabMeasurementEvaluator(
            measurement,
            passingLabEvaluationFunction,
            ALWAYS_VALID_DATE,
            ALWAYS_VALID_DATE
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
        val labValue: LabValue = LabTestFactory.forMeasurement(measurement).date(TEST_DATE).build()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(LabTestFactory.withLabValue(labValue)))
    }

    @Test
    fun canIgnoreOldDatesAndInvalidUnits() {
        val measurement = LabMeasurement.ALBUMIN
        val function = LabMeasurementEvaluator(
            measurement,
            passingLabEvaluationFunction,
            TEST_DATE,
            TEST_DATE
        )
        val wrongUnit: LabValue = LabTestFactory.builder().code(measurement.code()).date(TEST_DATE).build()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(LabTestFactory.withLabValue(wrongUnit)))
        val oldDate: LabValue = LabTestFactory.forMeasurement(measurement).date(TEST_DATE.minusDays(1)).build()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(LabTestFactory.withLabValue(oldDate)))
    }

    @Test
    fun warnsInCaseMeasurementIsOlderThanPassDate() {
        val measurement = LabMeasurement.ALBUMIN
        val function = LabMeasurementEvaluator(
            measurement,
            passingLabEvaluationFunction,
            TEST_DATE.minusDays(20),
            TEST_DATE.plusDays(20)
        )
        val warnDate: LabValue = LabTestFactory.forMeasurement(measurement).date(TEST_DATE).build()
        assertEvaluation(EvaluationResult.WARN, function.evaluate(LabTestFactory.withLabValue(warnDate)))
    }

    @Test
    fun canFallbackToSecondMostRecent() {
        val measurement = LabMeasurement.ALBUMIN
        val values: List<LabValue> = listOf(
            LabTestFactory.forMeasurement(measurement).date(TEST_DATE).build(),
            LabTestFactory.forMeasurement(measurement).date(TEST_DATE.minusDays(1)).build()
        )
        val record = LabTestFactory.withLabValues(values)
        val functionPass = LabMeasurementEvaluator(
            measurement,
            firstFailAndRestWithParam(EvaluationResult.PASS),
            ALWAYS_VALID_DATE,
            ALWAYS_VALID_DATE
        )
        assertEvaluation(EvaluationResult.WARN, functionPass.evaluate(record))
        val functionFail = LabMeasurementEvaluator(
            measurement,
            firstFailAndRestWithParam(EvaluationResult.FAIL),
            ALWAYS_VALID_DATE,
            ALWAYS_VALID_DATE
        )
        assertEvaluation(EvaluationResult.FAIL, functionFail.evaluate(record))
        val functionUndetermined = LabMeasurementEvaluator(
            measurement,
            firstFailAndRestWithParam(EvaluationResult.UNDETERMINED),
            ALWAYS_VALID_DATE,
            ALWAYS_VALID_DATE
        )
        assertEvaluation(EvaluationResult.FAIL, functionUndetermined.evaluate(record))
    }

    companion object {
        private val TEST_DATE = LocalDate.of(2020, 4, 20)
        private val ALWAYS_VALID_DATE = TEST_DATE.minusDays(2)

        private val passingLabEvaluationFunction: LabEvaluationFunction = object : LabEvaluationFunction {
            override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
                return EvaluationTestFactory.withResult(EvaluationResult.PASS)
            }
        }

        private fun firstFailAndRestWithParam(defaultEvaluation: EvaluationResult): LabEvaluationFunction {
            return object : LabEvaluationFunction {
                override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
                    return if (labValue.date() == TEST_DATE) {
                        EvaluationTestFactory.withResult(EvaluationResult.FAIL)
                    } else {
                        EvaluationTestFactory.withResult(defaultEvaluation)
                    }
                }
            }
        }
    }
}