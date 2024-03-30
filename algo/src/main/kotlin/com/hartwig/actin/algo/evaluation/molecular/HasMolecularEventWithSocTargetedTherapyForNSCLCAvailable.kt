package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.Displayable
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.trial.input.datamodel.VariantTypeInput

class HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(private val genesToIgnore: List<String>): EvaluationFunction {

    //TODO: include handling of warn evaluations of all nested evaluation rules below
    override fun evaluate(record: PatientRecord): Evaluation {
        val filteredVariantMap = targetableEventInNSCLCMap.mapValues { (_, variants) ->
            variants.filterNot { ValueComparison.stringCaseInsensitivelyMatchesQueryCollection(it.gene, genesToIgnore) }
        }

        val evaluations = filteredVariantMap.map { evaluateEvent(it.key, filteredVariantMap, record) }.flatMap { it ?: emptyList() }
        val passingVariants = evaluations.filter { it.second?.result == EvaluationResult.PASS }.map { it.first }
        val anyPass = passingVariants.isNotEmpty()

        return when {
            anyPass -> {
                val variantString = passingVariants
                    .joinToString(", ") { it.gene + (if (it.property.isNotEmpty()) " " else "") + it.property }
                EvaluationFactory.pass(
                    "Patient has molecular event(s) with SOC targeted therapy in NSCLC ($variantString)",
                    "Has molecular event(s) with SOC therapy in NSCLC ($variantString)"
                )
            } else -> {
                EvaluationFactory.fail(
                    "Does not have a molecular event with SOC therapy in NSCLC"
                )
            }
        }
    }

    private fun evaluateEvent(
        eventType: EventType, variantPropertyPairMap:  Map<EventType, List<VariantPropertyPair>>, record: PatientRecord): List<Pair<VariantPropertyPair, Evaluation?>>? {
        return variantPropertyPairMap[eventType]?.map { event ->
            val molecularRecord = record.molecular
            val evaluation = when (eventType) {
                EventType.ACTIVATING_VARIANT_IN_GENE -> molecularRecord?.let { GeneHasActivatingMutation(event.gene, null).evaluate(it) }
                EventType.EXON_SKIPPING -> {
                    molecularRecord?.let { GeneHasSpecificExonSkipping(event.gene, event.property.toInt()).evaluate(it) }
                }

                EventType.FUSIONS -> molecularRecord?.let { HasFusionInGene(event.gene).evaluate(it) }
                EventType.VARIANTS_WITH_PROTEIN_IMPACT -> {
                    molecularRecord?.let { GeneHasVariantWithProteinImpact(event.gene, listOf(event.property)).evaluate(it) }
                }

                EventType.DELETIONS, EventType.INSERTIONS -> {
                    molecularRecord?.let {
                        val exon = event.property.toInt()
                        GeneHasVariantInExonRangeOfType(event.gene, exon, exon,
                            if (eventType == EventType.DELETIONS) VariantTypeInput.DELETE else VariantTypeInput.INSERT
                        ).evaluate(it)
                    }
                }
            }
            event to evaluation
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

