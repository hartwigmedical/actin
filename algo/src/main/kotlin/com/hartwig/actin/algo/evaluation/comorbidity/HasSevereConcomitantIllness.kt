package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.interpretation.asText
import com.hartwig.actin.clinical.interpretation.isAtLeast
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult

class HasSevereConcomitantIllness : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val who = record.performanceStatus.latestWho

        return if (who?.let { who.isAtLeast(3) } == EvaluationResult.PASS) {
            EvaluationFactory.warn("Potentially has severe concomitant illnesses (WHO ${who.asText()})")
        } else {
            EvaluationFactory.fail("Assumed that patient has no severe concomitant illnesses")
        }
    }
}
