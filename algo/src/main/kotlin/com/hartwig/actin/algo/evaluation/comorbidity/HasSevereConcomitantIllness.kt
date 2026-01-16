package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.interpretation.asText
import com.hartwig.actin.clinical.interpretation.isAtMost
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision

class HasSevereConcomitantIllness : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val whoStatus = record.performanceStatus.latestWho
        val notEvaluatedResult = EvaluationFactory.notEvaluated("Assumed that severe concomitant illnesses are not present")
        return when {
            whoStatus == null -> notEvaluatedResult

            whoStatus.isAtMost(2) -> notEvaluatedResult

            whoStatus.precision != WhoStatusPrecision.EXACT -> EvaluationFactory.undetermined(
                "Unable to determine severe concomitant illnesses (exact WHO not available)"
            )

            whoStatus.status == 3 || whoStatus.status == 4 -> EvaluationFactory.warn(
                "Potential severe concomitant illnesses (WHO ${whoStatus.asText()})"
            )

            whoStatus.status == 5 -> EvaluationFactory.pass("WHO 5")

            else -> notEvaluatedResult
        }
    }

}
