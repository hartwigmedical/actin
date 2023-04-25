package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class PositiveForCD8TCellsByIHC internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return unrecoverable()
            .result(EvaluationResult.UNDETERMINED)
            .addUndeterminedSpecificMessages("It is currently undetermined if the sample is positive for CD8 T-Cells")
            .addUndeterminedGeneralMessages("Undetermined CD8 T-cell positivity status")
            .build()
    }
}