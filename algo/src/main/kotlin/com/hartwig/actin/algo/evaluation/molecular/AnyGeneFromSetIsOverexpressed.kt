package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.MolecularEvent
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.molecular.MolecularTest

class AnyGeneFromSetIsOverexpressed(
    private val genes: Set<String>,
    private val geneIsAmplifiedCreator: (String) -> GeneIsAmplified = { gene -> GeneIsAmplified(gene, null) }
) : MolecularEvaluationFunction() {

    private val genesToAmplification: Map<String, GeneIsAmplified> = genes.associateWith { geneIsAmplifiedCreator(it) }

    override fun evaluate(test: MolecularTest, ihcTests: List<IhcTest>): Evaluation {
        val amplifiedGenesWithEvents = genesToAmplification.mapValues { (_, geneIsAmplified) -> geneIsAmplified.evaluate(test, ihcTests) }
            .filterValues { it.result == EvaluationResult.PASS || it.result == EvaluationResult.WARN }
            .mapValues { it.value.inclusionMolecularEvents }

        return if (amplifiedGenesWithEvents.isNotEmpty()) {
            EvaluationFactory.warn(
                "(Possible) amplification of ${concat(amplifiedGenesWithEvents.keys)} detected and therefore possible overexpression in RNA",
                isMissingMolecularResultForEvaluation = true,
                inclusionEvents = amplifiedGenesWithEvents.flatMap { (gene, events) ->
                    events.map { MolecularEvent(it.event, "Potential $gene overexpression") }
                }.toSet()
            )
        } else {
            EvaluationFactory.undetermined(
                "Overexpression of ${concat(genes)} in RNA undetermined",
                isMissingMolecularResultForEvaluation = true
            )
        }
    }
}