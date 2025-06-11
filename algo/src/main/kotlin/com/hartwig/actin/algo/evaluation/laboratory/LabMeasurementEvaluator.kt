package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.evaluateInvalidLabValue
import com.hartwig.actin.algo.evaluation.util.Format.date
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationMessage
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.StaticMessage
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import java.time.LocalDate

class LabMeasurementEvaluator(
    private val measurement: LabMeasurement, private val function: LabEvaluationFunction,
    private val minValidDate: LocalDate, private val minPassDate: LocalDate, private val highestFirst: Boolean = true
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val interpretation = LabInterpretation.interpret(record.labValues)
        val mostRecent = interpretation.mostRecentValue(measurement, highestFirst)
        if (!LabEvaluation.isValid(mostRecent, measurement, minValidDate)) {
            return evaluateInvalidLabValue(measurement, mostRecent, minValidDate)
        }

        val evaluation = function.evaluate(record, measurement, mostRecent!!)

        return if (evaluation.result == EvaluationResult.PASS && !mostRecent.date.isAfter(minPassDate)) {
            Evaluation(
                result = EvaluationResult.PASS,
                recoverable = true,
                passMessages = appendPastMinPassDate(evaluation.passMessages).toSet()
            )
        } else evaluation
    }

    private fun appendPastMinPassDate(inputs: Set<EvaluationMessage>): List<StaticMessage> {
        return inputs.map { StaticMessage("$it but measurement occurred before ${date(minValidDate)}") }
    }
}