package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.interpretation.asText
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasSevereConcomitantIllness : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val whoStatus = record.performanceStatus.latestWho

        return if (whoStatus?.let { it.status >= 3 } == true) {
            EvaluationFactory.warn("Potentially has severe concomitant illnesses (WHO ${whoStatus.asText()})")
        } else {
            EvaluationFactory.fail("Assumed that patient has no severe concomitant illnesses")
        }
    }
}
