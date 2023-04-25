package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasPSMAPositivePETScan internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return unrecoverable()
            .result(EvaluationResult.UNDETERMINED)
            .addUndeterminedSpecificMessages("PSMA PET scan results currently cannot be determined")
            .addUndeterminedGeneralMessages("Positive PSMA PET unknown")
            .build()
    }
}