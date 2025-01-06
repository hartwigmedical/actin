package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.Format.percentage
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import java.time.LocalDate

class GeneHasVariantInCodon(private val gene: String, private val codons: List<String>, maxTestAge: LocalDate? = null) :
    MolecularEvaluationFunction(maxTestAge) {

    override fun genes() = listOf(gene)

    override fun evaluate(test: MolecularTest): Evaluation {
        val canonicalReportableVariantMatches: MutableSet<String> = mutableSetOf()
        val canonicalReportableSubclonalVariantMatches: MutableSet<String> = mutableSetOf()
        val canonicalUnreportableVariantMatches: MutableSet<String> = mutableSetOf()
        val canonicalCodonMatches: MutableSet<String> = mutableSetOf()
        val canonicalReportableSubclonalCodonMatches: MutableSet<String> = mutableSetOf()
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
                                canonicalReportableSubclonalCodonMatches.add(codon)
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

        return when {
            canonicalReportableVariantMatches.isNotEmpty() && reportableOtherVariantMatches.isEmpty() && canonicalReportableSubclonalVariantMatches.isEmpty() -> {
                EvaluationFactory.pass(
                    "Variant(s) in codon(s) ${concat(canonicalCodonMatches)} in $gene in canonical transcript",
                    inclusionEvents = canonicalReportableVariantMatches
                )
            }

            canonicalReportableVariantMatches.isNotEmpty() -> {
                val (specificExtension, generalExtension) = extendedWarnings(
                    reportableOtherVariantMatches,
                    canonicalReportableSubclonalVariantMatches,
                    reportableOtherCodonMatches,
                    canonicalReportableSubclonalCodonMatches
                )
                EvaluationFactory.warn(
                    "Variant(s) ${concat(canonicalReportableVariantMatches)} in codon(s) ${concat(canonicalCodonMatches)} in $gene in canonical transcript together with " + specificExtension,
                    inclusionEvents = canonicalReportableVariantMatches + reportableOtherVariantMatches + canonicalReportableSubclonalVariantMatches,
                )
            }

            else -> {
                val potentialWarnEvaluation = evaluatePotentialWarns(
                    canonicalReportableSubclonalVariantMatches,
                    canonicalUnreportableVariantMatches,
                    canonicalCodonMatches,
                    reportableOtherVariantMatches,
                    reportableOtherCodonMatches
                )

                potentialWarnEvaluation ?: EvaluationFactory.fail("No variants in codon(s) ${concat(codons)} in $gene")
            }
        }
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
                    "Variant(s) in codon(s) ${concat(canonicalReportableSubclonalVariantMatches)} in $gene in canonical transcript"
                            + " but subclonal likelihood of > ${percentage(1 - CLONAL_CUTOFF)}"
                ),
                EventsWithMessages(
                    canonicalUnreportableVariantMatches,
                    "Variant(s) in codon(s) ${concat(canonicalCodonMatches)} in $gene in canonical transcript but not considered reportable"
                ),
                EventsWithMessages(
                    reportableOtherVariantMatches,
                    "Variant(s) in codon(s) ${concat(reportableOtherCodonMatches)} in $gene but in non-canonical transcript"
                )
            )
        )
    }

    private fun extendedWarnings(
        reportableOtherVariantMatches: Set<String>,
        canonicalReportableSubclonalVariantMatches: Set<String>,
        reportableOtherCodonMatches: Set<String>,
        canonicalReportableSubclonalCodonMatches: Set<String>
    ): Pair<String, String> {
        val specificGeneralMessagePairs = listOfNotNull(
            if (reportableOtherVariantMatches.isNotEmpty()) {
                Pair(
                    "variant(s) ${concat(reportableOtherVariantMatches)} in codon(s) ${concat(reportableOtherCodonMatches)} but in non-canonical transcript",
                    "variant(s) ${concat(reportableOtherVariantMatches)} in codon(s) ${concat(reportableOtherCodonMatches)} but in non-canonical transcript"
                )
            } else null,
            if (canonicalReportableSubclonalVariantMatches.isNotEmpty()) {
                Pair(
                    "variant(s) ${concat(canonicalReportableSubclonalVariantMatches)} in codon(s) ${
                        concat(
                            canonicalReportableSubclonalCodonMatches
                        )
                    } in canonical transcript"
                            + " but subclonal likelihood of > ${percentage(1 - CLONAL_CUTOFF)}",
                    "variant(s) ${concat(canonicalReportableSubclonalVariantMatches)} in codon(s) ${
                        concat(
                            canonicalReportableSubclonalCodonMatches
                        )
                    } but subclonal likelihood of > "
                            + percentage(1 - CLONAL_CUTOFF)
                )
            } else null
        )
        return Pair(concat(specificGeneralMessagePairs.map { it.first }), concat(specificGeneralMessagePairs.map { it.second }))
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