package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class GeneIsOverexpressed internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return unrecoverable()
            .result(EvaluationResult.FAIL)
            .addFailSpecificMessages("RNA gene expression not yet evaluated")
            .addFailGeneralMessages("RNA gene expression not yet evaluated")
            .build()
    }
}