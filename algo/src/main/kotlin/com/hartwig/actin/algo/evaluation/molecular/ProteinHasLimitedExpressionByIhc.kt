package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class ProteinHasLimitedExpressionByIhc(
    private val protein: String,
    private val maxExpressionLevel: Int,
    private val labels: EvaluationLabels.Molecular
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return ProteinExpressionByIhcFunctions(protein, maxExpressionLevel, IhcExpressionComparisonType.LIMITED, labels).evaluate(record)
    }
}