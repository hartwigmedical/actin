package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.trial.input.datamodel.VariantTypeInput

class HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(private val genesToIgnore: List<String>): EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val filteredVariantMap = NSCLC_SOC_TARGETABLE_VARIANTS.mapValues { (_, variants) ->
            variants.filterNot { ValueComparison.stringCaseInsensitivelyMatchesQueryCollection(it, genesToIgnore) }
        }

        val hasActivatingMutation = evaluateEvent("Activating variant in gene", filteredVariantMap, record)
        val hasExonSkipping = evaluateEvent("Exon skipping", filteredVariantMap, record)
        val hasFusions = evaluateEvent("Fusions", filteredVariantMap, record)
        val hasSpecificVariants = evaluateEvent("Variants with protein impact", filteredVariantMap, record)
        val hasDeletions = evaluateEvent("Deletions", filteredVariantMap, record)
        val hasInsertions = evaluateEvent("Insertions", filteredVariantMap, record)

        val anyPass = listOf(hasActivatingMutation, hasExonSkipping, hasFusions, hasSpecificVariants, hasDeletions, hasInsertions)
            .any { !it.isNullOrEmpty() }

        return when {
            anyPass -> {
                EvaluationFactory.pass(
                    "Patient has a molecular event with SOC targeted therapy in NSCLC",
                    "Has molecular event with SOC therapy in NSCLC"
                )
            } else -> {
                EvaluationFactory.fail(
                    "Does not have a molecular event with SOC therapy in NSCLC"
                )
            }
        }
    }

    private fun evaluateEvent(eventType: String, variantMap: Map<String, List<String>>, record: PatientRecord): List<Evaluation>? {
        return variantMap[eventType]?.mapNotNull { event ->
            val molecularRecord = record.molecular
            when (eventType) {
                "Activating variant in gene" -> molecularRecord?.let { GeneHasActivatingMutation(event, null).evaluate(it) }

                "Exon skipping" -> {
                    val parts = event.split(" ")
                    molecularRecord?.let {
                        parts.let { parts -> GeneHasSpecificExonSkipping(parts.first(), parts.elementAt(1).toInt()).evaluate(it) }
                    }
                }

                "Fusions" -> molecularRecord?.let { HasFusionInGene(event).evaluate(it) }

                "Variants with protein impact" -> {
                    val parts = event.split(" ")
                    molecularRecord?.let { parts.let {
                            parts -> GeneHasVariantWithProteinImpact(parts.first(), listOf(parts.elementAt(1))).evaluate(it) }
                    }
                }

                "Deletions", "Insertions" -> {
                    val parts = event.split(" ")
                    molecularRecord?.let { parts.let { parts -> val exon = parts.elementAt(1).toInt()
                        GeneHasVariantInExonRangeOfType(
                            parts.first(), exon, exon,
                            if (eventType == "Deletions") {
                                VariantTypeInput.DELETE
                            } else VariantTypeInput.INSERT
                        ).evaluate(it) } }
                }
                else -> null
            }
        }?.filter { it.result == EvaluationResult.PASS }
    }

    companion object {
        val NSCLC_SOC_TARGETABLE_VARIANTS =
            mapOf(
                "Deletions" to listOf("EGFR 19"),
                "Insertions" to listOf("EGFR 20"),
                "Variants with protein impact" to listOf("EGFR L858R", "BRAF V600E"),
                "Activating variant in gene" to listOf("EGFR"),
                "Fusions" to listOf("ROS1", "ALK", "RET", "NTRK1", "NTRK2", "NTRK3"),
                "Exon skipping" to listOf("MET 14")
            )
    }
}

