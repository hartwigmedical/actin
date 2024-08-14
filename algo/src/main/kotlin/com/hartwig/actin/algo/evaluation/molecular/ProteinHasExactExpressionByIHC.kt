package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class ProteinHasExactExpressionByIHC(private val protein: String, private val expressionLevel: Int) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return ProteinExpressionByIHCFunction(protein, expressionLevel, IhcExpressionComparisonType.EXACT).evaluate(record)
    }
}