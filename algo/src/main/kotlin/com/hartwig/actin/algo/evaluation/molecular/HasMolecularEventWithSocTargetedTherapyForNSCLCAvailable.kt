package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.trial.input.datamodel.VariantTypeInput

class HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(
    private val genesToInclude: Set<String>? = null, private val genesToIgnore: Set<String>
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluationFunctions = createEvaluationFunctions(genesToInclude, genesToIgnore)
        return Or(evaluationFunctions).evaluate(record).copy(inclusionMolecularEvents = emptySet(), exclusionMolecularEvents = emptySet())
    }

    private fun createEvaluationFunctions(genesToInclude: Set<String>?, genesToIgnore: Set<String>): List<EvaluationFunction> =
        listOf(
            listOf(Triple("EGFR", "19", VariantTypeInput.DELETE), Triple("EGFR", "20", VariantTypeInput.INSERT))
                .map { (gene, exon, variantType) ->
                    gene to GeneHasVariantInExonRangeOfType(
                        gene,
                        exon.toInt(),
                        exon.toInt(),
                        variantType
                    )
                },
            listOf(Pair("EGFR", "L858R"), Pair("BRAF", "V600E")).map { (gene, impact) ->
                gene to GeneHasVariantWithProteinImpact(gene, listOf(impact))
            },
            listOf("EGFR").map { it to GeneHasActivatingMutation(it, null) },
            listOf("ROS1", "ALK", "RET", "NTRK1", "NTRK2", "NTRK3").map { it to HasFusionInGene(it) },
            listOf("MET" to GeneHasSpecificExonSkipping("MET", 14))
        ).flatten().filter {
            (gene, _) -> !genesToIgnore.contains(gene) || genesToInclude?.let { genesToInclude.contains(gene) } ?: false
        }.map { it.second }
}

