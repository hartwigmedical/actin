package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import java.time.LocalDate

class AnyGeneFromSetIsOverexpressed(
    maxTestAge: LocalDate? = null,
    private val genes: Set<String>,
    private val geneIsAmplifiedCreator: (String, LocalDate?) -> GeneIsAmplified = { gene, maxAge -> GeneIsAmplified(gene, null, maxAge) }
) : MolecularEvaluationFunction(maxTestAge) {

    private val genesToAmplification: Map<String, GeneIsAmplified> = genes.associateWith { geneIsAmplifiedCreator(it, maxTestAge) }

    override fun noMolecularRecordEvaluation(): Evaluation {
        return EvaluationFactory.undetermined(
            "No molecular data to determine overexpression of ${concat(genes)} in RNA",
            isMissingMolecularResultForEvaluation = true
        )
    }

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val amplifiedGenes = genesToAmplification.filter { (_, geneIsAmplified) ->
            val result = geneIsAmplified.evaluate(molecular)?.result
            result == EvaluationResult.PASS || result == EvaluationResult.WARN
        }.map { it.key }

        if (amplifiedGenes.isNotEmpty()) return EvaluationFactory.warn(
            "${concat(amplifiedGenes)} is amplified therefore possible overexpression in RNA",
            isMissingMolecularResultForEvaluation = true
        )

        return EvaluationFactory.undetermined(
            "Overexpression of ${concat(genes)} in RNA undetermined",
            isMissingMolecularResultForEvaluation = true
        )
    }
}