package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.evaluateInvalidLabValue
import com.hartwig.actin.algo.evaluation.util.Format.date
import com.hartwig.actin.clinical.interpretation.LabInterpreter
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import java.time.LocalDate

class LabMeasurementEvaluator(
    private val measurement: LabMeasurement, private val function: LabEvaluationFunction,
    private val minValidDate: LocalDate, private val minPassDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val interpretation = LabInterpreter.interpret(record.labValues)
        val mostRecent = interpretation.mostRecentValue(measurement)
        if (!LabEvaluation.isValid(mostRecent, measurement, minValidDate)) {
            evaluateInvalidLabValue(measurement, mostRecent, minValidDate)
        }
        
        val evaluation = function.evaluate(record, measurement, mostRecent!!)

        return if (evaluation.result == EvaluationResult.PASS && !mostRecent.date.isAfter(minPassDate)) {
            Evaluation(
                result = EvaluationResult.WARN,
                recoverable = true,
                warnSpecificMessages = appendPastMinPassDate(evaluation.passSpecificMessages).toSet()
            )
        } else evaluation
    }

    private fun appendPastMinPassDate(inputs: Set<String>): List<String> {
        return inputs.map { "$it, but measurement occurred before ${date(minValidDate)}" }
    }
}