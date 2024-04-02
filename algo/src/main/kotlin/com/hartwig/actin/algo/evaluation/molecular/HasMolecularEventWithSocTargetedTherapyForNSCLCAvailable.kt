package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.Displayable
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.trial.input.datamodel.VariantTypeInput

class HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(private val genesToIgnore: List<String>): EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val filteredVariantMap = targetableEventInNSCLCMap.mapValues { (_, variants) ->
            variants.filterNot { ValueComparison.stringCaseInsensitivelyMatchesQueryCollection(it.gene, genesToIgnore) }
        }

        val eventToEvaluationList = filteredVariantMap
            .map { eventToEvaluationFunction(it.key, filteredVariantMap) }.flatMap { it ?: emptyList() }
        val evaluationFunctions = eventToEvaluationList.map { it.second }

        val passGeneralMessages = createMessage(record, evaluationFunctions, isSpecific = false, EvaluationResult.PASS)
        val passSpecificMessages = createMessage(record, evaluationFunctions, isSpecific = true, EvaluationResult.PASS)
        val warnGeneralMessages = createMessage(record, evaluationFunctions, isSpecific = false, EvaluationResult.WARN)
        val warnSpecificMessages = createMessage(record, evaluationFunctions, isSpecific = true, EvaluationResult.WARN)
        val failGeneralMessages = createMessage(record, evaluationFunctions, isSpecific = false, EvaluationResult.FAIL)
        val failSpecificMessages = createMessage(record, evaluationFunctions, isSpecific = true, EvaluationResult.FAIL)

        return when {
            passGeneralMessages.isNotEmpty() || passSpecificMessages.isNotEmpty() -> {
                EvaluationFactory.pass(passSpecificMessages, passGeneralMessages)
            }
            warnGeneralMessages.isNotEmpty() || warnSpecificMessages.isNotEmpty() -> {
                EvaluationFactory.warn(warnSpecificMessages, warnGeneralMessages)
            } else -> EvaluationFactory.fail(failSpecificMessages, failGeneralMessages)
        }
    }



    private fun eventToEvaluationFunction(
        eventType: EventType, eventMap:  Map<EventType, List<VariantPropertyPair>>): List<Pair<VariantPropertyPair, EvaluationFunction>>? {
        return eventMap[eventType]?.map { event ->
            val evaluationFunction = when (eventType) {
                EventType.ACTIVATING_VARIANT_IN_GENE -> GeneHasActivatingMutation(event.gene, null)
                EventType.EXON_SKIPPING -> GeneHasSpecificExonSkipping(event.gene, event.property.toInt())
                EventType.FUSIONS -> HasFusionInGene(event.gene)
                EventType.VARIANTS_WITH_PROTEIN_IMPACT -> GeneHasVariantWithProteinImpact(event.gene, listOf(event.property))
                EventType.DELETIONS, EventType.INSERTIONS -> {
                    val exon = event.property.toInt()
                    GeneHasVariantInExonRangeOfType(event.gene, exon, exon,
                        if (eventType == EventType.DELETIONS) VariantTypeInput.DELETE else VariantTypeInput.INSERT
                    )
                    }
                }
            event to evaluationFunction
        }
    }

    private fun createMessage(
        record: PatientRecord, evaluationFunctions: List<EvaluationFunction>, isSpecific: Boolean, resultType: EvaluationResult
    ): String {
        val evaluations = Or(evaluationFunctions).evaluate(record)
        return when (resultType) {
            EvaluationResult.PASS -> {
                (if (isSpecific) evaluations.passSpecificMessages else evaluations.passGeneralMessages).joinToString(", ")
            }
            EvaluationResult.WARN -> {
                (if (isSpecific) evaluations.warnSpecificMessages else evaluations.warnGeneralMessages).joinToString(", ")
            }
            else -> {
                (if (isSpecific) evaluations.failSpecificMessages else evaluations.failGeneralMessages).joinToString(", ")
            }
        }
    }

    companion object {
        internal enum class EventType(private val display: String) : Displayable {
            DELETIONS("Deletions"),
            INSERTIONS("Insertions"),
            VARIANTS_WITH_PROTEIN_IMPACT("Variants with protein impact"),
            ACTIVATING_VARIANT_IN_GENE("Activating variant in gene"),
            FUSIONS("Fusions"),
            EXON_SKIPPING("Exon skipping");

            override fun display(): String {
                return display
            }
        }

        internal data class VariantPropertyPair (val gene: String, val property: String = "") {
            val variant = Pair(gene, property)
        }

        internal val targetableEventInNSCLCMap =
            mapOf(
                EventType.DELETIONS to listOf(VariantPropertyPair("EGFR", "19")),
                EventType.INSERTIONS to listOf(VariantPropertyPair("EGFR", "20")),
                EventType.VARIANTS_WITH_PROTEIN_IMPACT to listOf(VariantPropertyPair("EGFR", "L858R"), VariantPropertyPair("BRAF", "V600E")),
                EventType.ACTIVATING_VARIANT_IN_GENE to listOf(VariantPropertyPair("EGFR")),
                EventType.FUSIONS to listOf(
                    VariantPropertyPair("ROS1"),
                    VariantPropertyPair("ALK"),
                    VariantPropertyPair("RET"),
                    VariantPropertyPair("NTRK1"),
                    VariantPropertyPair("NTRK2"),
                    VariantPropertyPair("NTRK3")
                ),
                EventType.EXON_SKIPPING to listOf(VariantPropertyPair("MET", "14"))
            )
    }
}

