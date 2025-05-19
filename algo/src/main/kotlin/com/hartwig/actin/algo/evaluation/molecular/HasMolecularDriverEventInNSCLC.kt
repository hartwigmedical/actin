package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import java.time.LocalDate

private val ACTIVATING_MUTATION_LIST = listOf("EGFR", "ERBB2")
private val PROTEIN_IMPACT_LIST = listOf(Pair("BRAF", "V600E"), Pair("KRAS", "G12C"))
private val FUSION_LIST = listOf("ALK", "NRG1", "NTRK1", "NTRK2", "NTRK3", "RET", "ROS1")
private val EXON_SKIPPING_LIST = listOf(Pair("MET", 14))
private val ALL_GENES =
    listOf(ACTIVATING_MUTATION_LIST, PROTEIN_IMPACT_LIST.map { it.first }, FUSION_LIST, EXON_SKIPPING_LIST.map { it.first }).flatten()
        .distinct()

class HasMolecularDriverEventInNSCLC(
    private val genesToInclude: Set<String>? = null,
    private val genesToIgnore: Set<String>,
    private val maxTestAge: LocalDate? = null,
    private val includeGenesAtLeast: Boolean? = false
) : MolecularEvaluationFunction(maxTestAge) {

    override fun evaluate(record: PatientRecord): Evaluation {
        if (includeGenesAtLeast == true && genesToInclude != null) {
            val evaluation = Or(createEvaluationFunctions(null, genesToIgnore)).evaluate(record)
            val genesToWarn = ALL_GENES - genesToInclude
            return if (hasEvaluationEventInGenes(evaluation, genesToWarn) && !hasEvaluationEventInGenes(
                    evaluation,
                    genesToInclude.toList()
                )
            )
                evaluation.copy(
                    result = EvaluationResult.WARN,
                    warnMessages = setOf("Undetermined if patient's molecular driver event is applicable as 'molecular driver event' in NSCLC"),
                    passMessages = emptySet(),
                    inclusionMolecularEvents = emptySet(),
                    exclusionMolecularEvents = emptySet(),
                    isMissingMolecularResultForEvaluation = evaluation.isMissingMolecularResultForEvaluation
                ) else evaluation.copy(
                inclusionMolecularEvents = emptySet(),
                exclusionMolecularEvents = emptySet(),
                isMissingMolecularResultForEvaluation = evaluation.isMissingMolecularResultForEvaluation
            )
        } else {
            val evaluation = Or(createEvaluationFunctions(genesToInclude, genesToIgnore)).evaluate(record)
            return evaluation.copy(
                inclusionMolecularEvents = emptySet(),
                exclusionMolecularEvents = emptySet(),
                isMissingMolecularResultForEvaluation = evaluation.isMissingMolecularResultForEvaluation
            )
        }
    }

    private fun createEvaluationFunctions(genesToInclude: Set<String>?, genesToIgnore: Set<String>): List<EvaluationFunction> =
        listOf(
            ACTIVATING_MUTATION_LIST.map { it to GeneHasActivatingMutation(it, null, maxTestAge) },
            PROTEIN_IMPACT_LIST.map { (gene, impact) -> gene to GeneHasVariantWithProteinImpact(gene, setOf(impact), maxTestAge) },
            FUSION_LIST.map { it to HasFusionInGene(it, maxTestAge) },
            EXON_SKIPPING_LIST.map { (gene, exon) -> gene to GeneHasSpecificExonSkipping(gene, exon, maxTestAge) }
        ).flatten().filter { (gene, _) ->
            genesToInclude?.contains(gene) ?: !genesToIgnore.contains(gene)
        }.map { it.second }

    private fun hasEvaluationEventInGenes(evaluation: Evaluation, genesList: List<String>): Boolean {
        return genesList.any { gene ->
            evaluation.inclusionMolecularEvents.any { string -> string.contains(gene) }
        } && (evaluation.result == EvaluationResult.PASS || evaluation.result == EvaluationResult.WARN)
    }
}