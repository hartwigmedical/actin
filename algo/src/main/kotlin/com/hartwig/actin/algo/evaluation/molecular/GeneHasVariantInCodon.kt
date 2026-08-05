package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.Format.concatVariants
import com.hartwig.actin.algo.evaluation.util.Format.percentage
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.driver.Variant

class GeneHasVariantInCodon(override val gene: String, private val codons: List<String>, labels: EvaluationLabels.Molecular) :
    MolecularEvaluationFunction(
        targetCoveragePredicate = specific(MolecularTestTarget.MUTATION, labels.geneHasVariantInCodonMessagePrefix(codons)),
        labels = labels
    ) {

    private enum class VariantClassification {
        CANONICAL_REPORTABLE,
        CANONICAL_REPORTABLE_SUBCLONAL,
        CANONICAL_UNREPORTABLE,
        REPORTABLE_OTHER,
        NONE
    }

    override fun evaluate(test: MolecularTest): Evaluation {
        val canonicalCodonMatches = mutableSetOf<String>()
        val canonicalReportableSubclonalCodonMatches = mutableSetOf<String>()
        val reportableOtherCodonMatches = mutableSetOf<String>()

        val variantClassifications = test.drivers.variants.filter { it.gene == gene }
            .onEach { variant ->
                codons.forEach { codon ->
                    if (isCodonMatch(variant.canonicalImpact.affectedCodon, codon)) {
                        canonicalCodonMatches.add(codon)
                        if (variant.isReportable && variant.clonalLikelihood?.let { it < CLONAL_CUTOFF } == true) {
                            canonicalReportableSubclonalCodonMatches.add(codon)
                        }
                    }
                    if (variant.isReportable) {
                        variant.otherImpacts.forEach {
                            if (isCodonMatch(it.affectedCodon, codon)) reportableOtherCodonMatches.add(codon)
                        }
                    }
                }
            }
            .groupBy { variant ->
                val hasCanonicalCodonMatch = containsCodon(variant.canonicalImpact.affectedCodon, canonicalCodonMatches)
                when {
                    hasCanonicalCodonMatch && variant.isReportable && variant.clonalLikelihood?.let { it < CLONAL_CUTOFF } == true -> {
                        VariantClassification.CANONICAL_REPORTABLE_SUBCLONAL
                    }

                    hasCanonicalCodonMatch && variant.isReportable -> {
                        VariantClassification.CANONICAL_REPORTABLE
                    }

                    hasCanonicalCodonMatch -> {
                        VariantClassification.CANONICAL_UNREPORTABLE
                    }

                    variant.isReportable && variant.otherImpacts.any { containsCodon(it.affectedCodon, reportableOtherCodonMatches) } -> {
                        VariantClassification.REPORTABLE_OTHER
                    }

                    else -> VariantClassification.NONE
                }
            }.mapValues { (_, variants) -> variants.map(Variant::event).toSet() }

        val canonicalReportableVariantMatches = variantClassifications[VariantClassification.CANONICAL_REPORTABLE] ?: emptySet()
        val canonicalReportableSubclonalVariantMatches =
            variantClassifications[VariantClassification.CANONICAL_REPORTABLE_SUBCLONAL] ?: emptySet()
        val canonicalUnreportableVariantMatches = variantClassifications[VariantClassification.CANONICAL_UNREPORTABLE] ?: emptySet()
        val reportableOtherVariantMatches = variantClassifications[VariantClassification.REPORTABLE_OTHER] ?: emptySet()

        return when {
            canonicalReportableVariantMatches.isNotEmpty() && reportableOtherVariantMatches.isEmpty() && canonicalReportableSubclonalVariantMatches.isEmpty() -> {
                EvaluationFactory.pass(
                    labels.geneHasVariantInCodonPass(
                        concatVariants(canonicalReportableVariantMatches, gene), concat(canonicalCodonMatches), gene
                    ),
                    inclusionEvents = canonicalReportableVariantMatches
                )
            }

            canonicalReportableVariantMatches.isNotEmpty() -> {
                val extension = extendedWarnings(
                    reportableOtherVariantMatches,
                    canonicalReportableSubclonalVariantMatches,
                    reportableOtherCodonMatches,
                    canonicalReportableSubclonalCodonMatches
                )
                EvaluationFactory.warn(
                    labels.geneHasVariantInCodonWarnExtended(
                        concatVariants(canonicalReportableVariantMatches, gene), concat(canonicalCodonMatches), gene, extension
                    ),
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

                potentialWarnEvaluation ?: EvaluationFactory.fail(
                    labels.geneHasVariantInCodonFail(Format.concatWithCommaAndOr(codons), gene)
                )
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
                    labels.geneHasVariantInCodonWarnSubclonal(
                        concatVariants(canonicalReportableSubclonalVariantMatches, gene), gene, percentage(1 - CLONAL_CUTOFF)
                    )
                ),
                EventsWithMessages(
                    canonicalUnreportableVariantMatches,
                    labels.geneHasVariantInCodonWarnUnreportable(concat(canonicalCodonMatches), gene)
                ),
                EventsWithMessages(
                    reportableOtherVariantMatches,
                    labels.geneHasVariantInCodonWarnNonCanonical(concat(reportableOtherCodonMatches), gene)
                )
            )
        )
    }

    private fun extendedWarnings(
        reportableOtherVariantMatches: Set<String>,
        canonicalReportableSubclonalVariantMatches: Set<String>,
        reportableOtherCodonMatches: Set<String>,
        canonicalReportableSubclonalCodonMatches: Set<String>
    ): String {
        val message = listOfNotNull(
            if (reportableOtherVariantMatches.isNotEmpty()) {
                labels.geneHasVariantInCodonExtensionNonCanonical(
                    concatVariants(reportableOtherVariantMatches, gene), concat(reportableOtherCodonMatches)
                )
            } else null,
            if (canonicalReportableSubclonalVariantMatches.isNotEmpty()) {
                labels.geneHasVariantInCodonExtensionSubclonal(
                    concatVariants(canonicalReportableSubclonalVariantMatches, gene),
                    concat(canonicalReportableSubclonalCodonMatches),
                    percentage(1 - CLONAL_CUTOFF)
                )
            } else null
        )
        return concat(message)
    }

    private fun isCodonMatch(affectedCodon: Int?, codonToMatch: String): Boolean {
        if (affectedCodon == null) {
            return false
        }
        val codonIndexToMatch = codonToMatch.substring(1).toInt()
        return codonIndexToMatch == affectedCodon
    }

    private fun containsCodon(affectedCodon: Int?, codonsToMatch: Set<String>): Boolean {
        return codonsToMatch.any { it.substring(1).toInt() == affectedCodon }
    }

    companion object {
        private const val CLONAL_CUTOFF = 0.5
    }
}