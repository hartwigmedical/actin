package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class MolecularResultsAreGenerallyAvailable internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return unrecoverable()
            .result(EvaluationResult.NOT_EVALUATED)
            .addPassSpecificMessages("It is assumed that there are molecular results available if applicable")
            .build()
    }
}