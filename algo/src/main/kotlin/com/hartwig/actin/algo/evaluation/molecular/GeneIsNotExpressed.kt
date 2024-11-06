package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import java.time.LocalDate

class GeneIsNotExpressed(maxTestAge: LocalDate? = null, private val gene: String) : MolecularEvaluationFunction(maxTestAge){
    
    override fun evaluate(molecular: MolecularRecord): Evaluation {
        return EvaluationFactory.undetermined("Non-expression of $gene in RNA undetermined", missingGenesForEvaluation = true)
    }
}