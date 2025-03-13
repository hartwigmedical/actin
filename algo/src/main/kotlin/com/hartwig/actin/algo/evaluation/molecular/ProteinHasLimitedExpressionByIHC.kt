package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class ProteinHasLimitedExpressionByIHC(private val protein: String, private val gene: String, private val maxExpressionLevel: Int, private val maxTestAge: LocalDate? = null) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return ProteinExpressionByIHCFunctions(protein, gene, maxExpressionLevel, IhcExpressionComparisonType.LIMITED, maxTestAge).evaluate(record)
    }
}