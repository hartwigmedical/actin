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
            delInsEvaluation, proteinImpactVariantEvaluation, activatingMutationEvaluation, fusionEvaluation, exonSkippingEvaluation
        ).flatten().filter {
            (gene, _) -> !genesToIgnore.contains(gene) || genesToInclude?.let { genesToInclude.contains(gene) } ?: false
        }.map { it.second }

    private val delInsEvaluation =
        listOf(Triple("EGFR", "19", VariantTypeInput.DELETE), Triple("EGFR", "20", VariantTypeInput.INSERT))
            .map { (gene, exon, variantType) -> gene to GeneHasVariantInExonRangeOfType(gene, exon.toInt(), exon.toInt(), variantType) }
    private val proteinImpactVariantEvaluation =
        listOf(Pair("EGFR", "L858R"), Pair("BRAF", "V600E"))
            .map { (gene, impact) -> gene to GeneHasVariantWithProteinImpact(gene, listOf(impact)) }
    private val activatingMutationEvaluation = listOf("EGFR").map { it to GeneHasActivatingMutation(it, null) }
    private val fusionEvaluation = listOf("ROS1", "ALK", "RET", "NTRK1", "NTRK2", "NTRK3").map { it to HasFusionInGene(it) }
    private val exonSkippingEvaluation = listOf("MET" to GeneHasSpecificExonSkipping("MET", 14))
}

