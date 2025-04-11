package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class ProteinHasSufficientExpressionByIHC(private val protein: String, private val minExpressionLevel: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return ProteinExpressionByIHCFunctions(protein, minExpressionLevel, IhcExpressionComparisonType.SUFFICIENT).evaluate(record)
    }
}