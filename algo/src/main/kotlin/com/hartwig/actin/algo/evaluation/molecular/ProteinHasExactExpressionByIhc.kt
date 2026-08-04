package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class ProteinHasExactExpressionByIhc(
    private val protein: String,
    private val expressionLevel: Int,
    private val labels: EvaluationLabels.Molecular
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return ProteinExpressionByIhcFunctions(protein, expressionLevel, IhcExpressionComparisonType.EXACT, labels).evaluate(record)
    }
}