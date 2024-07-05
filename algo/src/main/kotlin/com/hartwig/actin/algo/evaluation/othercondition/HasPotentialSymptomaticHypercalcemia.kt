package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation
import com.hartwig.actin.clinical.interpretation.LabInterpreter
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.clinical.interpretation.LabMeasurement.CALCIUM
import com.hartwig.actin.clinical.interpretation.LabMeasurement.IONIZED_CALCIUM
import java.time.LocalDate

class HasPotentialSymptomaticHypercalcemia(private val minValidDate: LocalDate) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val calciumEvaluation = evaluateLabValue(record, CALCIUM)
        val ionizedCalciumEvaluation = evaluateLabValue(record, IONIZED_CALCIUM)
        val correctedCalciumEvaluation = evaluateLabValue(record, IONIZED_CALCIUM)
        val evaluations = listOf(calciumEvaluation, ionizedCalciumEvaluation, correctedCalciumEvaluation)

        return when {
            evaluations.any { it == LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN } -> {
                EvaluationFactory.warn(
                    "Patient may have symptomatic hypercalcemia (calcium above ULN)",
                    "Possible symptomatic hypercalcemia (calcium above ULN)"
                )
            }
            evaluations.any { it == LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED } -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient may have symptomatic hypercalcemia",
                    "Undetermined symptomatic hypercalcemia"
                )
            }
            else -> {
                val messageStart = "No indications for possible symptomatic hypercalcemia"
                EvaluationFactory.recoverableFail("$messageStart (calcium within ULN)", messageStart)
            }
        }
    }

    private fun evaluateLabValue(record: PatientRecord, measurement: LabMeasurement): LabEvaluation.LabEvaluationResult {
        val interpretation = LabInterpreter.interpret(record.labValues)
        val mostRecent = interpretation.mostRecentValue(measurement)
        return if (LabEvaluation.isValid(value, measurement, minValidDate) && mostRecent != null) {
            LabEvaluation.evaluateVersusMaxULN(mostRecent, 1.0)
        } else LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED
    }
}