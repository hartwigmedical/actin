package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class ProteinHasSufficientExpressionByIHC(private val protein: String, private val gene: String, private val minExpressionLevel: Int, private val maxTestAge: LocalDate? = null) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return ProteinExpressionByIHCFunctions(protein, gene, minExpressionLevel, IhcExpressionComparisonType.SUFFICIENT, maxTestAge).evaluate(record)
    }
}