package com.hartwig.actin.algo.evaluation.molecular

import com.google.common.collect.Sets
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.Format.percentage

class GeneHasVariantInCodon internal constructor(private val gene: String, private val codons: List<String>) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val canonicalReportableVariantMatches: MutableSet<String> = Sets.newHashSet()
        val canonicalReportableSubclonalVariantMatches: MutableSet<String> = Sets.newHashSet()
        val canonicalUnreportableVariantMatches: MutableSet<String> = Sets.newHashSet()
        val canonicalCodonMatches: MutableSet<String> = Sets.newHashSet()
        val reportableOtherVariantMatches: MutableSet<String> = Sets.newHashSet()
        val reportableOtherCodonMatches: MutableSet<String> = Sets.newHashSet()
        for (variant in record.molecular().drivers().variants()) {
            if (variant.gene() == gene) {
                for (codon in codons) {
                    if (isCodonMatch(variant.canonicalImpact().affectedCodon(), codon)) {
                        canonicalCodonMatches.add(codon)
                        if (variant.isReportable) {
                            if (variant.clonalLikelihood() < CLONAL_CUTOFF) {
                                canonicalReportableSubclonalVariantMatches.add(variant.event())
                            } else {
                                canonicalReportableVariantMatches.add(variant.event())
                            }
                        } else {
                            canonicalUnreportableVariantMatches.add(variant.event())
                        }
                    }
                    if (variant.isReportable) {
                        for (otherImpact in variant.otherImpacts()) {
                            if (isCodonMatch(otherImpact.affectedCodon(), codon)) {
                                reportableOtherVariantMatches.add(variant.event())
                                reportableOtherCodonMatches.add(codon)
                            }
                        }
                    }
                }
            }
        }
        if (canonicalReportableVariantMatches.isNotEmpty()) {
            return unrecoverable()
                .result(EvaluationResult.PASS)
                .addAllInclusionMolecularEvents(canonicalReportableVariantMatches)
                .addPassSpecificMessages(
                    "Variant(s) in codon(s) " + concat(canonicalCodonMatches) + " in gene " + gene
                            + " detected in canonical transcript"
                )
                .addPassGeneralMessages("Variant(s) in codon(s) " + concat(canonicalCodonMatches) + " found in " + gene)
                .build()
        }
        val potentialWarnEvaluation = evaluatePotentialWarns(
            canonicalReportableSubclonalVariantMatches,
            canonicalUnreportableVariantMatches,
            canonicalCodonMatches,
            reportableOtherVariantMatches,
            reportableOtherCodonMatches
        )
        return potentialWarnEvaluation
            ?: unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No variants in codon(s) " + concat(codons) + " detected in gene " + gene)
                .addFailGeneralMessages("No specific variants in codon(s) in $gene detected")
                .build()
    }

    private fun evaluatePotentialWarns(
        canonicalReportableSubclonalVariantMatches: Set<String>,
        canonicalUnreportableVariantMatches: Set<String>, canonicalCodonMatches: Set<String>,
        reportableOtherVariantMatches: Set<String>, reportableOtherCodonMatches: Set<String>
    ): Evaluation? {
        val warnEvents: MutableSet<String> = Sets.newHashSet()
        val warnSpecificMessages: MutableSet<String> = Sets.newHashSet()
        val warnGeneralMessages: MutableSet<String> = Sets.newHashSet()
        if (canonicalReportableSubclonalVariantMatches.isNotEmpty()) {
            warnEvents.addAll(canonicalReportableSubclonalVariantMatches)
            warnSpecificMessages.add(
                "Variant(s) in codon(s) " + concat(canonicalReportableSubclonalVariantMatches) + " in " + gene
                        + " detected in canonical transcript, but subclonal likelihood of > " + percentage(1 - CLONAL_CUTOFF)
            )
            warnGeneralMessages.add(
                "Variant(s) in codon(s) " + concat(canonicalReportableSubclonalVariantMatches) + " found in " + gene
                        + " but subclonal likelihood of > " + percentage(1 - CLONAL_CUTOFF)
            )
        }
        if (canonicalUnreportableVariantMatches.isNotEmpty()) {
            warnEvents.addAll(canonicalUnreportableVariantMatches)
            warnSpecificMessages.add(
                "Variant(s) in codon(s) " + concat(canonicalCodonMatches) + " in " + gene
                        + " detected in canonical transcript, but not considered reportable"
            )
            warnGeneralMessages.add(
                "Variant(s) in codon(s) " + concat(canonicalCodonMatches) + " found in canonical transcript of gene " + gene
            )
        }
        if (reportableOtherVariantMatches.isNotEmpty()) {
            warnEvents.addAll(reportableOtherVariantMatches)
            warnSpecificMessages.add(
                "Variant(s) in codon(s) " + concat(reportableOtherCodonMatches) + " in " + gene
                        + " detected, but in non-canonical transcript"
            )
            warnGeneralMessages.add(
                "Variant(s) in codon(s) " + concat(canonicalCodonMatches) + " found in non-canonical transcript of gene "
                        + gene
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
        private const val CLONAL_CUTOFF = 0.5
        private fun isCodonMatch(affectedCodon: Int?, codonToMatch: String): Boolean {
            if (affectedCodon == null) {
                return false
            }
            val codonIndexToMatch = codonToMatch.substring(1).toInt()
            return codonIndexToMatch == affectedCodon
        }
    }
}