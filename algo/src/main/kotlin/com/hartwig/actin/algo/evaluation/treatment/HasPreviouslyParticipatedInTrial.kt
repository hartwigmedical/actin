package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasPreviouslyParticipatedInTrial : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.oncologicalHistory.any { it.isTrial }) {
            EvaluationFactory.pass("Has participated in trial")
        } else {
            EvaluationFactory.fail("Has not participated in trial")
        }
    }
}