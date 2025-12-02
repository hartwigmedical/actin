package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularTest

class AnyGeneFromSetIsOverexpressed(
    private val genes: Set<String>,
    private val geneIsAmplifiedCreator: (String) -> GeneIsAmplified = { gene -> GeneIsAmplified(gene, null) }
) : MolecularEvaluationFunction() {

    private val genesToAmplification: Map<String, GeneIsAmplified> = genes.associateWith { geneIsAmplifiedCreator(it) }

    override fun evaluate(test: MolecularTest): Evaluation {
        val amplifiedGenes = genesToAmplification.filter { (_, geneIsAmplified) ->
            val result = geneIsAmplified.evaluate(test).result
            result == EvaluationResult.PASS || result == EvaluationResult.WARN
        }.map { it.key }

        return if (amplifiedGenes.isNotEmpty()) {
            EvaluationFactory.warn(
                "Amplification of ${concat(amplifiedGenes)} detected and therefore possible overexpression in RNA",
                isMissingMolecularResultForEvaluation = true,
                inclusionEvents = amplifiedGenes.map { "Potential $it overexpression" }.toSet()
            )
        } else {
            EvaluationFactory.undetermined(
                "Overexpression of ${concat(genes)} in RNA undetermined",
                isMissingMolecularResultForEvaluation = true
            )
        }
    }
}