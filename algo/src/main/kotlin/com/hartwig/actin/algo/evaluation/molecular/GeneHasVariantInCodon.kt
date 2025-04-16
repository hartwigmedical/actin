package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.Format.concatVariants
import com.hartwig.actin.algo.evaluation.util.Format.percentage
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.driver.Variant
import java.time.LocalDate

class GeneHasVariantInCodon(private val gene: String, private val codons: List<String>, maxTestAge: LocalDate? = null) :
    MolecularEvaluationFunction(maxTestAge) {

    private enum class VariantClassification {
        CANONICAL_REPORTABLE,
        CANONICAL_REPORTABLE_SUBCLONAL,
        CANONICAL_UNREPORTABLE,
        REPORTABLE_OTHER,
        NONE
    }

    override fun genes() = listOf(gene)
    override fun targetCoveragePredicate() = atLeast(MolecularTestTarget.MUTATION)

    override fun evaluate(test: MolecularTest): Evaluation {
        val canonicalCodonMatches = mutableSetOf<String>()
        val canonicalReportableSubclonalCodonMatches = mutableSetOf<String>()
        val reportableOtherCodonMatches = mutableSetOf<String>()

        val variantClassifications = test.drivers.variants.filter { it.gene == gene }
            .onEach { variant ->
                codons.forEach { codon ->
                    if (isCodonMatch(variant.canonicalImpact.affectedCodon, codon)) {
                        canonicalCodonMatches.add(codon)
                        if (variant.isReportable && variant.extendedVariantDetails?.clonalLikelihood?.let { it < CLONAL_CUTOFF } == true) {
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
                    hasCanonicalCodonMatch && variant.isReportable && variant.extendedVariantDetails?.clonalLikelihood?.let { it < CLONAL_CUTOFF } == true -> {
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
                    "Variant(s) ${concatVariants(canonicalReportableVariantMatches, gene)} in codon(s) " +
                            "${concat(canonicalCodonMatches)} in $gene in canonical transcript",
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
                    "Variant(s) ${concatVariants(canonicalReportableVariantMatches, gene)} in codon(s) ${
                        concat(
                            canonicalCodonMatches
                        )
                    } in $gene in canonical transcript together with " + extension,
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
                    "No variants in codon(s) ${Format.concatWithCommaAndOr(codons)} in $gene"
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
                    "Variant(s) in codon(s) ${concatVariants(canonicalReportableSubclonalVariantMatches, gene)} in $gene in " +
                            "canonical transcript but subclonal likelihood of > ${percentage(1 - CLONAL_CUTOFF)}"
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
    ): String {
        val message = listOfNotNull(
            if (reportableOtherVariantMatches.isNotEmpty()) {
                "variant(s) ${concatVariants(reportableOtherVariantMatches, gene)} in codon(s) ${
                    concat(reportableOtherCodonMatches)
                } but in non-canonical transcript"
            } else null,
            if (canonicalReportableSubclonalVariantMatches.isNotEmpty()) {
                "variant(s) ${concatVariants(canonicalReportableSubclonalVariantMatches, gene)} in codon(s) ${
                    concat(canonicalReportableSubclonalCodonMatches)
                } in canonical transcript" + " but subclonal likelihood of > ${percentage(1 - CLONAL_CUTOFF)}"
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