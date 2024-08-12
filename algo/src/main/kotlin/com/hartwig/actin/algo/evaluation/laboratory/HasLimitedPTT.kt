package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasLimitedPTT: EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.recoverableUndeterminedNoGeneral("PTT cannot be determined yet")
    }
}