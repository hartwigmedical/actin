package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.StaticMessage
import java.time.LocalDate

private val ACTIVATING_MUTATION_LIST = listOf("EGFR", "ERBB2")
private val PROTEIN_IMPACT_LIST = listOf("BRAF" to "V600E", "KRAS" to "G12C")
private val FUSION_LIST = listOf("ALK", "NRG1", "NTRK1", "NTRK2", "NTRK3", "RET", "ROS1")
private val EXON_SKIPPING_LIST = listOf("MET" to 14)
private val ALL_GENES =
    ACTIVATING_MUTATION_LIST + PROTEIN_IMPACT_LIST.map { it.first } + FUSION_LIST + EXON_SKIPPING_LIST.map { it.first }.distinct()

class HasMolecularDriverEventInNsclc(
    private val genesToInclude: Set<String>?,
    private val genesToExclude: Set<String>,
    private val maxTestAge: LocalDate? = null,
    private val warnForMatchesOutsideGenesToInclude: Boolean,
    private val withAvailableSoc: Boolean,
) : MolecularEvaluationFunction(maxTestAge) {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (warnForMatchesOutsideGenesToInclude && genesToInclude != null) {
            val evaluation = Or(createEvaluationFunctions(null, genesToExclude)).evaluate(record)
            if (hasPositiveEvaluationEventInGenes(evaluation, ALL_GENES - genesToInclude) && !hasPositiveEvaluationEventInGenes(
                    evaluation,
                    genesToInclude.toList()
                )
            ) {
                clearMolecularEventsAndConfigureMessages(evaluation, true)
            } else {
                clearMolecularEventsAndConfigureMessages(evaluation, false)
            }
        } else {
            val evaluation = Or(createEvaluationFunctions(genesToInclude, genesToExclude)).evaluate(record)
            clearMolecularEventsAndConfigureMessages(evaluation, false)
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

    private fun hasPositiveEvaluationEventInGenes(evaluation: Evaluation, genesList: List<String>): Boolean {
        return genesList.any { gene ->
            evaluation.inclusionMolecularEvents.any { string -> string.contains(gene) }
        } && (evaluation.result == EvaluationResult.PASS || evaluation.result == EvaluationResult.WARN)
    }

    private fun clearMolecularEventsAndConfigureMessages(evaluation: Evaluation, mustWarn: Boolean = false): Evaluation {
        val soc = if (withAvailableSoc) " with available SOC" else ""
        val message = "NSCLC driver event(s)$soc detected: ${Format.concat(evaluation.inclusionMolecularEvents)}"
        return evaluation.copy(
            result = if (mustWarn) EvaluationResult.WARN else evaluation.result,
            passMessages = writePassMessage(evaluation.passMessagesStrings(), mustWarn, message),
            warnMessages = writeWarnMessage(evaluation.passMessagesStrings(), evaluation.warnMessagesStrings(), mustWarn, message),
            failMessages = writeFailMessage(evaluation.failMessagesStrings()),
            inclusionMolecularEvents = emptySet(),
            exclusionMolecularEvents = emptySet(),
        )
    }

    private fun writePassMessage(passInput: Set<String>, mustWarn: Boolean, message: String): Set<StaticMessage> {
        return if (mustWarn || passInput.isEmpty()) emptySet() else setOf(StaticMessage(message))
    }

    private fun writeWarnMessage(passInput: Set<String>, warnInput: Set<String>, mustWarn: Boolean, message: String): Set<StaticMessage> {
        return when {
            mustWarn && passInput.isNotEmpty() -> setOf(StaticMessage("Potential $message (but undetermined if applicable)"))

            warnInput.isNotEmpty() -> setOf(StaticMessage("Potential $message"))

            else -> emptySet()
        }
    }

    private fun writeFailMessage(failInput: Set<String>): Set<StaticMessage> {
        return if (failInput.isEmpty()) emptySet() else setOf(StaticMessage("No (applicable) NSCLC driver event(s) detected"))
    }
}