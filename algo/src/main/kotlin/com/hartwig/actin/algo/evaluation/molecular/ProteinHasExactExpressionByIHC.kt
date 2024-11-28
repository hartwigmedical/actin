package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class ProteinHasExactExpressionByIHC(private val protein: String, private val expressionLevel: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return ProteinExpressionByIHCFunctions(protein, expressionLevel, IhcExpressionComparisonType.EXACT).evaluate(record)
    }
}