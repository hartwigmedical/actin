package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasHistologicalDocumentationOfTumorType internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.notEvaluated(
            "Currently it is assumed that histological documentation of tumor type has been done or can be done",
            "Histological documentation assumed"
        )
    }
}