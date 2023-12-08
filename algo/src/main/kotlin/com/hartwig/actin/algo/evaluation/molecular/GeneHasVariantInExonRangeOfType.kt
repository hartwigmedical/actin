package com.hartwig.actin.algo.evaluation.molecular

import com.google.common.collect.Sets
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.molecular.datamodel.driver.VariantType
import com.hartwig.actin.treatment.input.datamodel.VariantTypeInput
import org.apache.logging.log4j.util.Strings

class GeneHasVariantInExonRangeOfType(
    private val gene: String, private val minExon: Int, private val maxExon: Int,
    private val requiredVariantType: VariantTypeInput?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val exonRangeMessage = generateExonRangeMessage(minExon, maxExon)
        val variantTypeMessage = generateRequiredVariantTypeMessage(requiredVariantType)
        val allowedVariantTypes = determineAllowedVariantTypes(requiredVariantType)
        val canonicalReportableVariantMatches: MutableSet<String> = Sets.newHashSet()
        val canonicalUnreportableVariantMatches: MutableSet<String> = Sets.newHashSet()
        val reportableOtherVariantMatches: MutableSet<String> = Sets.newHashSet()
        for (variant in record.molecular().drivers().variants()) {
            if (variant.gene() == gene && allowedVariantTypes.contains(variant.type())) {
                if (hasEffectInExonRange(variant.canonicalImpact().affectedExon(), minExon, maxExon)) {
                    if (variant.isReportable) {
                        canonicalReportableVariantMatches.add(variant.event())
                    } else {
                        canonicalUnreportableVariantMatches.add(variant.event())
                    }
                }
                if (variant.isReportable) {
                    for (otherImpact in variant.otherImpacts()) {
                        if (hasEffectInExonRange(otherImpact.affectedExon(), minExon, maxExon)) {
                            reportableOtherVariantMatches.add(variant.event())
                        }
                    }
                }
            }
        }
        if (canonicalReportableVariantMatches.isNotEmpty()) {
            return unrecoverable()
                .result(EvaluationResult.PASS)
                .addAllInclusionMolecularEvents(canonicalReportableVariantMatches)
                .addPassSpecificMessages("Variant(s) in exon $exonRangeMessage in gene $gene$variantTypeMessage detected in canonical transcript")
                .addPassGeneralMessages("Variant(s) in exon $exonRangeMessage in gene $gene$variantTypeMessage detected")
                .build()
        }
        val potentialWarnEvaluation =
            evaluatePotentialWarns(canonicalUnreportableVariantMatches, reportableOtherVariantMatches, exonRangeMessage, variantTypeMessage)
        return potentialWarnEvaluation
            ?: unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No variant in exon $exonRangeMessage detected in gene $gene$variantTypeMessage detected in canonical transcript")
                .addFailGeneralMessages("No variant in exon $exonRangeMessage in gene $gene$variantTypeMessage detected")
                .build()
    }

    private fun evaluatePotentialWarns(
        canonicalUnreportableVariantMatches: Set<String>,
        reportableOtherVariantMatches: Set<String>,
        exonRangeMessage: String,
        variantTypeMessage: String
    ): Evaluation? {
        val warnEvents: MutableSet<String> = Sets.newHashSet()
        val warnSpecificMessages: MutableSet<String> = Sets.newHashSet()
        val warnGeneralMessages: MutableSet<String> = Sets.newHashSet()
        if (canonicalUnreportableVariantMatches.isNotEmpty()) {
            warnEvents.addAll(canonicalUnreportableVariantMatches)
            warnSpecificMessages.add(
                "Variant(s) in exon $exonRangeMessage in gene $gene$variantTypeMessage detected in canonical transcript but considered not reportable"
            )
            warnGeneralMessages.add(
                "Variant(s) in exon $exonRangeMessage in gene $gene$variantTypeMessage detected but not reportable"
            )
        }
        if (reportableOtherVariantMatches.isNotEmpty()) {
            warnEvents.addAll(reportableOtherVariantMatches)
            warnSpecificMessages.add(
                "Variant(s) in exon $exonRangeMessage in gene $gene$variantTypeMessage detected but in non-canonical transcript"
            )
            warnGeneralMessages.add(
                "Variant(s) in exon $exonRangeMessage in gene $gene$variantTypeMessage detected but in non-canonical transcript"
            )
        }
        return if (warnEvents.isNotEmpty() && warnSpecificMessages.isNotEmpty() && warnGeneralMessages.isNotEmpty()) {
            unrecoverable()
                .result(EvaluationResult.WARN)
                .addAllInclusionMolecularEvents(warnEvents)
                .addAllWarnSpecificMessages(warnSpecificMessages)
                .addAllWarnGeneralMessages(warnGeneralMessages)
                .build()
        } else null
    }

    companion object {
        private fun hasEffectInExonRange(affectedExon: Int?, minExon: Int, maxExon: Int): Boolean {
            return affectedExon != null && affectedExon >= minExon && affectedExon <= maxExon
        }

        private fun generateExonRangeMessage(minExon: Int, maxExon: Int): String {
            return if (minExon == maxExon) {
                minExon.toString()
            } else {
                "$minExon - $maxExon"
            }
        }

        private fun generateRequiredVariantTypeMessage(requiredVariantType: VariantTypeInput?): String {
            return if (requiredVariantType == null) {
                Strings.EMPTY
            } else when (requiredVariantType) {
                VariantTypeInput.SNV, VariantTypeInput.MNV, VariantTypeInput.INDEL -> {
                    " of type $requiredVariantType"
                }

                VariantTypeInput.INSERT -> {
                    " of type insertion"
                }

                VariantTypeInput.DELETE -> {
                    " of type deletion"
                }
            }
        }

        private fun determineAllowedVariantTypes(requiredVariantType: VariantTypeInput?): Set<VariantType> {
            return if (requiredVariantType == null) {
                Sets.newHashSet(*VariantType.values())
            } else when (requiredVariantType) {
                VariantTypeInput.SNV -> {
                    Sets.newHashSet(VariantType.SNV)
                }

                VariantTypeInput.MNV -> {
                    Sets.newHashSet(VariantType.MNV)
                }

                VariantTypeInput.INSERT -> {
                    Sets.newHashSet(VariantType.INSERT)
                }

                VariantTypeInput.DELETE -> {
                    Sets.newHashSet(VariantType.DELETE)
                }

                VariantTypeInput.INDEL -> {
                    Sets.newHashSet(VariantType.INSERT, VariantType.DELETE)
                }

                else -> {
                    throw IllegalStateException("Could not map required variant type: $requiredVariantType")
                }
            }
        }
    }
}