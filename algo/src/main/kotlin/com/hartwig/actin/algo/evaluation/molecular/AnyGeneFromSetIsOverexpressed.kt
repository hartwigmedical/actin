package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import java.time.LocalDate

class AnyGeneFromSetIsOverexpressed(maxTestAge: LocalDate? = null, private val genes: Set<String>) : MolecularEvaluationFunction(maxTestAge) {
    
    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val lastGene = genes.last()
        val geneString = genes.filterNot { lastGene == it }.joinToString(", ") + ", and $lastGene"
        return EvaluationFactory.undetermined("Overexpression of $geneString in RNA undetermined", missingGenesForEvaluation = true)
    }
}