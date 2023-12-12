package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.date
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabInterpreter
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import java.time.LocalDate

class LabMeasurementEvaluator(
    private val measurement: LabMeasurement, private val function: LabEvaluationFunction,
    private val minValidDate: LocalDate, private val minPassDate: LocalDate
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val interpretation = LabInterpreter.interpret(record.clinical().labValues())
        val mostRecent = interpretation.mostRecentValue(measurement)
        if (!isValid(mostRecent, measurement)) {
            val builder = recoverable().result(EvaluationResult.UNDETERMINED)
            if (mostRecent == null) {
                builder.addUndeterminedSpecificMessages("No measurement found for " + measurement.display())
            } else if (mostRecent.unit() != measurement.defaultUnit()) {
                builder.addUndeterminedSpecificMessages("Unexpected unit specified for " + measurement.display() + ": " + mostRecent.unit())
            } else if (mostRecent.date().isBefore(minValidDate)) {
                builder.addUndeterminedSpecificMessages("Most recent measurement too old for " + measurement.display())
            }
            return builder.build()
        }
        val evaluation = function.evaluate(record, measurement, mostRecent!!)
        if (evaluation.result() == EvaluationResult.FAIL) {
            val secondMostRecent = interpretation.secondMostRecentValue(measurement)
            if (isValid(secondMostRecent, measurement)) {
                val secondEvaluation = function.evaluate(record, measurement, secondMostRecent!!)
                if (secondEvaluation.result() == EvaluationResult.PASS) {
                    return recoverable()
                        .result(EvaluationResult.WARN)
                        .addWarnSpecificMessages("Latest measurement fails for " + measurement.display() + ", but second-latest succeeded")
                        .build()
                }
            }
        }
        return if (evaluation.result() == EvaluationResult.PASS && !mostRecent.date().isAfter(minPassDate)) {
            recoverable()
                .result(EvaluationResult.WARN)
                .addAllWarnSpecificMessages(appendPastMinPassDate(evaluation.passSpecificMessages()))
                .build()
        } else evaluation
    }

    private fun isValid(value: LabValue?, measurement: LabMeasurement): Boolean {
        return value != null && value.unit() == measurement.defaultUnit() && !value.date().isBefore(minValidDate)
    }

    private fun appendPastMinPassDate(inputs: Set<String>): List<String> {
        return inputs.map { it + ", but measurement occurred before " + date(minValidDate) }
    }
}