package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.isValid
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabMeasurement.CREATININE
import com.hartwig.actin.datamodel.clinical.LabMeasurement.CREATININE_URINE
import com.hartwig.actin.datamodel.clinical.LabMeasurement.URINE_VOLUME_24H
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate

class HasSufficientMeasuredCreatinineClearance(
    private val minCreatinineClearance: Double,
    private val minValidLabDate: LocalDate,
    private val minPassLabDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val interpreter = LabInterpretation.interpret(record.labValues)
        val urineVolume = interpreter.mostRecentValue(URINE_VOLUME_24H)
        val urineCreatinine = interpreter.mostRecentValue(CREATININE_URINE)
        val serumCreatinine = interpreter.mostRecentValue(CREATININE)
        val onSameDate = urineVolume?.date == urineCreatinine?.date && urineCreatinine?.date == serumCreatinine?.date
        if (!onSameDate || !checkValidity(urineVolume, URINE_VOLUME_24H) || !checkValidity(urineCreatinine, CREATININE_URINE) || !checkValidity(serumCreatinine, CREATININE)) {
            return EvaluationFactory.recoverableUndetermined("Values to calculate measured creatinine clearance not present or cannot be evaluated")
        }

        val value = calcMeasuredCrCl(urineVolume!!.value, urineCreatinine!!.value, serumCreatinine!!.value)
        val evaluations = setOf(evaluateVersusMinValue(value, ">", minCreatinineClearance))

        return when (val result = CreatinineFunctions.interpretEGFREvaluations(evaluations)) {
            EvaluationResult.FAIL -> {
                EvaluationFactory.recoverableFail("Measured creatinine clearance below min of $minCreatinineClearance")
            }

            EvaluationResult.UNDETERMINED -> {
                EvaluationFactory.recoverableUndetermined("Measured creatinine clearance evaluation undetermined")
            }

            EvaluationResult.PASS -> {
                EvaluationFactory.recoverablePass("Measured creatinine clearance above min of $minCreatinineClearance")
            }

            else -> {
                Evaluation(result = result, recoverable = true)
            }
        }
    }

    private fun calcMeasuredCrCl(urineVolume: Double, urineCreatinine: Double, serumCreatinine: Double): Double {
        return (urineCreatinine * urineVolume * 1000) / (serumCreatinine * 24 * 60)
    }

    private fun checkValidity(
        mostRecent: LabValue?, measurement: LabMeasurement
    ): Boolean {
        return isValid(mostRecent, measurement, minValidLabDate) && mostRecent?.date?.isAfter(minPassLabDate) == true
    }
}