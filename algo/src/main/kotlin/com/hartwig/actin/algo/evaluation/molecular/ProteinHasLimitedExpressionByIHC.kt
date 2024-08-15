package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class ProteinHasLimitedExpressionByIHC(private val protein: String, private val maxExpressionLevel: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return ProteinExpressionByIHCFunctions(protein, maxExpressionLevel, IhcExpressionComparisonType.LIMITED).evaluate(record)
    }
}