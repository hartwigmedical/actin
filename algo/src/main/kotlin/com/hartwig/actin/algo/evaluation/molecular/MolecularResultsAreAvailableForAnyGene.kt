package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class MolecularResultsAreAvailableForAnyGene(private val genes: Set<String>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluations = genes.map { gene -> MolecularResultsAreAvailableForGeneInteremediate(gene).evaluate(record) }

        val evaluationsByResult = evaluations.distinct().groupBy(IntermediateEvaluation::result)
        val best = evaluationsByResult.keys.maxOrNull()
        if (best == null) {
            throw IllegalStateException("Could not determine aggregate evaluations for MolecularResultsAreAvailableForAnyGene")
        } else {
            val aggregatedEvaluations = IntermediateEvaluation.aggregateToEvaluation(best, evaluations)
            if (aggregatedEvaluations.size != 1) {
                throw IllegalStateException("Expected unique aggregation message for MolecularResultsAreAvailableForAnyGene")
            }
            return aggregatedEvaluations.first()
        }
    }
}
