package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasFamilyHistoryOfIdiopathicSuddenDeath internal constructor() : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.recoverableUndetermined("Family history of idopathic sudden death undetermined")
    }
}