package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction

//TODO: Update according to README
class ProteinHasLimitedExpressionByIHCCreator internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return unrecoverable()
            .result(EvaluationResult.UNDETERMINED)
            .addUndeterminedSpecificMessages("Currently limited expression by IHC cannot be evaluated")
            .addUndeterminedGeneralMessages("Limited IHC gene expression currently undetermined")
            .build()
    }
}