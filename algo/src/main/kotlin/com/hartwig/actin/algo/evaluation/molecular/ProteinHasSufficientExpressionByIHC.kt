package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class ProteinHasSufficientExpressionByIHC(private val protein: String, private val minExpressionLevel: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return ProteinExpressionByIHCFunctions(protein, minExpressionLevel, IhcExpressionComparisonType.SUFFICIENT).evaluate(record)
    }
}