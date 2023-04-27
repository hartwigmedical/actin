package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

//TODO: Update according to README
class HasExtracranialMetastases internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Currently it not determined if there are extracranial metastases",
            "Undetermined extracranial metastases"
        )
    }
}