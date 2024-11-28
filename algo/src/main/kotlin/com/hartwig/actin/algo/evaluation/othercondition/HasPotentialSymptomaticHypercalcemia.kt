package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN
import com.hartwig.actin.clinical.interpretation.LabInterpreter
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.clinical.interpretation.LabMeasurement.CALCIUM
import com.hartwig.actin.clinical.interpretation.LabMeasurement.CORRECTED_CALCIUM
import com.hartwig.actin.clinical.interpretation.LabMeasurement.IONIZED_CALCIUM
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate

class HasPotentialSymptomaticHypercalcemia(private val minValidDate: LocalDate) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val interpretation = LabInterpreter.interpret(record.labValues)
        val evaluations = sequenceOf(CALCIUM, IONIZED_CALCIUM, CORRECTED_CALCIUM)
            .map { evaluateLabValue(interpretation.mostRecentValue(it), it) }
            .toSet()

        return when {
            EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN in evaluations || EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN in evaluations -> {
                EvaluationFactory.recoverableWarn(
                    "Patient may have symptomatic hypercalcemia (calcium above ULN)",
                    "Possible symptomatic hypercalcemia (calcium above ULN)"
                )
            }
            CANNOT_BE_DETERMINED in evaluations -> {
                EvaluationFactory.recoverableUndetermined(
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

    private fun evaluateLabValue(mostRecent: LabValue?, measurement: LabMeasurement): LabEvaluation.LabEvaluationResult {
        return if (LabEvaluation.isValid(mostRecent, measurement, minValidDate) && mostRecent != null) {
            LabEvaluation.evaluateVersusMaxULN(mostRecent, 1.0)
        } else LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED
    }
}