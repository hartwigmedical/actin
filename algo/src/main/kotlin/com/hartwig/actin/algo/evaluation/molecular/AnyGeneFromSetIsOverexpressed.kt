package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.MolecularEvent
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget

class AnyGeneFromSetIsOverexpressed(
    private val genes: Set<String>,
    labels: EvaluationLabels.Molecular,
    private val geneIsAmplifiedCreator: (String) -> GeneIsAmplified = { gene -> GeneIsAmplified(gene, null, labels) }
) : MolecularEvaluationFunction(labels = labels) {

    private val genesToAmplification: Map<String, GeneIsAmplified> = genes.associateWith { geneIsAmplifiedCreator(it) }

    override fun evaluate(test: MolecularTest, ihcTests: List<IhcTest>): Evaluation {
        val amplifiedGenesWithEvents = genesToAmplification.mapValues { (_, geneIsAmplified) -> geneIsAmplified.evaluate(test, ihcTests) }
            .filterValues { it.result == EvaluationResult.PASS || it.result == EvaluationResult.WARN }
            .mapValues { it.value.inclusionMolecularEvents }

        return if (amplifiedGenesWithEvents.isNotEmpty()) {
            EvaluationFactory.warn(
                labels.anyGeneFromSetIsOverexpressedWarn(concat(amplifiedGenesWithEvents.keys)),
                isMissingMolecularResultForEvaluation = true,
                inclusionEvents = amplifiedGenesWithEvents.flatMap { (gene, events) ->
                    events.map { MolecularEvent(it.event, "Potential $gene overexpression") }
                }.toSet()
            )
        } else {
            val (genesTestedForAmplificationInDna, genesNotTestedForAmplificationInDna) =
                genes.partition { test.testsGene(it, specific(MolecularTestTarget.AMPLIFICATION, labels.geneIsAmplifiedMessagePrefix())) }
            val dnaClarification = when {
                genesTestedForAmplificationInDna.isEmpty() -> ""
                genesNotTestedForAmplificationInDna.isEmpty() -> " (but no amplifications found in DNA)"
                else -> " (no amplification in DNA for ${concat(genesTestedForAmplificationInDna)})"
            }
            EvaluationFactory.undetermined(
                labels.anyGeneFromSetIsOverexpressedUndetermined(concat(genes), dnaClarification),
                isMissingMolecularResultForEvaluation = true
            )
        }
    }
}