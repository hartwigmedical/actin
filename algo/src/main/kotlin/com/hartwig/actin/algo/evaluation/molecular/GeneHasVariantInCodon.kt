package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.Format.percentage
import com.hartwig.actin.molecular.datamodel.MolecularTest

class GeneHasVariantInCodon(private val gene: String, private val codons: List<String>) : MolecularEvaluationFunction {

    override fun genes() = listOf(gene)

    override fun evaluate(test: MolecularTest): Evaluation {
        val canonicalReportableVariantMatches: MutableSet<String> = mutableSetOf()
        val canonicalReportableSubclonalVariantMatches: MutableSet<String> = mutableSetOf()
        val canonicalUnreportableVariantMatches: MutableSet<String> = mutableSetOf()
        val canonicalCodonMatches: MutableSet<String> = mutableSetOf()
        val reportableOtherVariantMatches: MutableSet<String> = mutableSetOf()
        val reportableOtherCodonMatches: MutableSet<String> = mutableSetOf()
        for (variant in test.drivers.variants) {
            if (variant.gene == gene) {
                for (codon in codons) {
                    if (isCodonMatch(variant.canonicalImpact.affectedCodon, codon)) {
                        canonicalCodonMatches.add(codon)
                        if (variant.isReportable) {
                            if (variant.extendedVariantDetails?.clonalLikelihood?.let { it < CLONAL_CUTOFF } == true) {
                                canonicalReportableSubclonalVariantMatches.add(variant.event)
                            } else {
                                canonicalReportableVariantMatches.add(variant.event)
                            }
                        } else {
                            canonicalUnreportableVariantMatches.add(variant.event)
                        }
                    }
                    if (variant.isReportable) {
                        for (otherImpact in variant.otherImpacts) {
                            if (isCodonMatch(otherImpact.affectedCodon, codon)) {
                                reportableOtherVariantMatches.add(variant.event)
                                reportableOtherCodonMatches.add(codon)
                            }
                        }
                    }
                }
            }
        }
        if (canonicalReportableVariantMatches.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Variant(s) in codon(s) ${concat(canonicalCodonMatches)} in gene $gene detected in canonical transcript",
                "Variant(s) in codon(s) ${concat(canonicalCodonMatches)} in $gene",
                inclusionEvents = canonicalReportableVariantMatches
            )
        }
        val potentialWarnEvaluation = evaluatePotentialWarns(
            canonicalReportableSubclonalVariantMatches,
            canonicalUnreportableVariantMatches,
            canonicalCodonMatches,
            reportableOtherVariantMatches,
            reportableOtherCodonMatches
        )
        return potentialWarnEvaluation ?: EvaluationFactory.fail(
            "No variants in codon(s) ${concat(codons)} detected in gene $gene", "No variants in codon(s) ${concat(codons)} in $gene"
        )
    }

    private fun evaluatePotentialWarns(
        canonicalReportableSubclonalVariantMatches: Set<String>,
        canonicalUnreportableVariantMatches: Set<String>, canonicalCodonMatches: Set<String>,
        reportableOtherVariantMatches: Set<String>, reportableOtherCodonMatches: Set<String>
    ): Evaluation? {
        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOf(
                EventsWithMessages(
                    canonicalReportableSubclonalVariantMatches,
                    "Variant(s) in codon(s) ${concat(canonicalReportableSubclonalVariantMatches)} in $gene detected in canonical transcript"
                            + " but subclonal likelihood of > ${percentage(1 - CLONAL_CUTOFF)}",
                    "Variant(s) in codon(s) ${concat(canonicalReportableSubclonalVariantMatches)} in $gene but subclonal likelihood of > "
                            + percentage(1 - CLONAL_CUTOFF)
                ),
                EventsWithMessages(
                    canonicalUnreportableVariantMatches,
                    "Variant(s) in codon(s) ${concat(canonicalCodonMatches)} in $gene detected in canonical transcript but not considered reportable",
                    "Variant(s) in codon(s) ${concat(canonicalCodonMatches)} in $gene but not reportable"
                ),
                EventsWithMessages(
                    reportableOtherVariantMatches,
                    "Variant(s) in codon(s) ${concat(reportableOtherCodonMatches)} in $gene detected but in non-canonical transcript",
                    "Variant(s) in codon(s) ${concat(canonicalCodonMatches)} in $gene but in non-canonical transcript"
                )
            )
        )
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