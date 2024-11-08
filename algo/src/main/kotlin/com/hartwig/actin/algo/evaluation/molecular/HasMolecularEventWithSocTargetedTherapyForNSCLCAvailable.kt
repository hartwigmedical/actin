package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.trial.input.datamodel.VariantTypeInput
import java.time.LocalDate

private val DEL_INS_LIST = listOf(Triple("EGFR", 19, VariantTypeInput.DELETE), Triple("EGFR", 20, VariantTypeInput.INSERT))
private val PROTEIN_IMPACT_LIST = listOf(Pair("EGFR", "L858R"), Pair("BRAF", "V600E"))
private val ACTIVATING_MUTATION_LIST = listOf("EGFR")
private val FUSION_LIST = listOf("ROS1", "ALK", "RET", "NTRK1", "NTRK2", "NTRK3")
private val EXON_SKIPPING_LIST = listOf("MET" to 14)

class HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(
    private val genesToInclude: Set<String>? = null, private val genesToIgnore: Set<String>, private val maxTestAge: LocalDate? = null
) : MolecularEvaluationFunction(maxTestAge) {

    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluation = Or(createEvaluationFunctions(genesToInclude, genesToIgnore)).evaluate(record)
        return evaluation.copy(
            inclusionMolecularEvents = emptySet(),
            exclusionMolecularEvents = emptySet(),
            isMissingGenesForSufficientEvaluation = evaluation.isMissingGenesForSufficientEvaluation
        )
    }

    private fun createEvaluationFunctions(genesToInclude: Set<String>?, genesToIgnore: Set<String>): List<EvaluationFunction> =
        listOf(
            DEL_INS_LIST.map { (gene, exon, variantType) ->
                gene to GeneHasVariantInExonRangeOfType(
                    gene,
                    exon,
                    exon,
                    variantType,
                    maxTestAge
                )
            },
            PROTEIN_IMPACT_LIST.map { (gene, impact) -> gene to GeneHasVariantWithProteinImpact(gene, listOf(impact), maxTestAge) },
            ACTIVATING_MUTATION_LIST.map { it to GeneHasActivatingMutation(it, null, maxTestAge) },
            FUSION_LIST.map { it to HasFusionInGene(it, maxTestAge) },
            EXON_SKIPPING_LIST.map { it.first to GeneHasSpecificExonSkipping(it.first, it.second, maxTestAge) }
        ).flatten().filter { (gene, _) ->
            !genesToIgnore.contains(gene) || genesToInclude?.let { genesToInclude.contains(gene) } ?: false
        }.map { it.second }
}

