package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasFamilyHistoryOfIdiopathicSuddenDeath internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.recoverableUndetermined(
            "Currently undetermined if patient has family history of idiopathic sudden death",
            "Undetermined family history of idopathic sudden death"
        )
    }
}