package com.hartwig.actin.algo.evaluation.molecular

import com.google.common.annotations.VisibleForTesting
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.Format.percentage
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.TranscriptImpact
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import java.time.LocalDate
import org.apache.logging.log4j.LogManager

class GeneHasVariantWithProteinImpact(
    private val gene: String, private val allowedProteinImpacts: List<String>,
    recencyCutoff: LocalDate?
) : MolecularEvaluationFunction(recencyCutoff) {

    override fun genes() = listOf(gene)

    override fun evaluate(test: MolecularTest): Evaluation {
        val canonicalReportableVariantMatches: MutableSet<String> = mutableSetOf()
        val canonicalReportableSubclonalVariantMatches: MutableSet<String> = mutableSetOf()
        val canonicalUnreportableVariantMatches: MutableSet<String> = mutableSetOf()
        val canonicalProteinImpactMatches: MutableSet<String> = mutableSetOf()
        val reportableOtherVariantMatches: MutableSet<String> = mutableSetOf()
        val reportableOtherProteinImpactMatches: MutableSet<String> = mutableSetOf()

        for (variant in test.drivers.variants) {
            if (variant.gene == gene) {
                val canonicalProteinImpact = toProteinImpact(variant.canonicalImpact.hgvsProteinImpact)
                for (allowedProteinImpact in allowedProteinImpacts) {
                    if (canonicalProteinImpact == allowedProteinImpact) {
                        canonicalProteinImpactMatches.add(allowedProteinImpact)
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
                        for (otherProteinImpact in toProteinImpacts(variant.otherImpacts)) {
                            if (otherProteinImpact == allowedProteinImpact) {
                                reportableOtherVariantMatches.add(variant.event)
                                reportableOtherProteinImpactMatches.add(allowedProteinImpact)
                            }
                        }
                    }
                }
            }
        }
        if (canonicalReportableVariantMatches.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Variant(s) ${concat(canonicalProteinImpactMatches)} in gene $gene detected in canonical transcript",
                "${concat(canonicalProteinImpactMatches)} detected in $gene",
                inclusionEvents = canonicalReportableVariantMatches
            )
        }
        val potentialWarnEvaluation = evaluatePotentialWarns(
            canonicalReportableSubclonalVariantMatches,
            canonicalUnreportableVariantMatches,
            canonicalProteinImpactMatches,
            reportableOtherVariantMatches,
            reportableOtherProteinImpactMatches
        )
        return potentialWarnEvaluation ?: EvaluationFactory.fail(
            "None of ${concat(allowedProteinImpacts)} detected in gene $gene", "${concat(allowedProteinImpacts)} not detected in $gene"
        )
    }

    private fun evaluatePotentialWarns(
        canonicalReportableSubclonalVariantMatches: Set<String>,
        canonicalUnreportableVariantMatches: Set<String>, canonicalProteinImpactMatches: Set<String>,
        reportableOtherVariantMatches: Set<String>, reportableOtherProteinImpactMatches: Set<String>
    ): Evaluation? {
        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOf(
                EventsWithMessages(
                    canonicalReportableSubclonalVariantMatches,
                    "Variant(s) ${concat(canonicalReportableSubclonalVariantMatches)} in $gene"
                            + " detected in canonical transcript but subclonal likelihood of > " + percentage(1 - CLONAL_CUTOFF),
                    "Variant(s) ${concat(canonicalReportableSubclonalVariantMatches)} in $gene but subclonal likelihood of > "
                            + percentage(1 - CLONAL_CUTOFF)
                ),
                EventsWithMessages(
                    canonicalUnreportableVariantMatches,
                    "Variant(s) ${concat(canonicalProteinImpactMatches)} in $gene detected in canonical transcript but are not reportable",
                    "${concat(canonicalProteinImpactMatches)} found in $gene but not reportable"
                ),
                EventsWithMessages(
                    reportableOtherVariantMatches,
                    "Variant(s) ${concat(reportableOtherProteinImpactMatches)} in $gene detected but in non-canonical transcript",
                    "${concat(reportableOtherProteinImpactMatches)} found in non-canonical transcript of gene $gene"
                )
            )
        )
    }

    companion object {
        private val LOGGER = LogManager.getLogger(
            GeneHasVariantWithProteinImpact::class.java
        )
        private const val CLONAL_CUTOFF = 0.5
        private fun toProteinImpacts(impacts: Set<TranscriptImpact>): Set<String> {
            return impacts.map { toProteinImpact(it.hgvsProteinImpact) }.toSet()
        }

        @VisibleForTesting
        fun toProteinImpact(hgvsProteinImpact: String): String {
            val impact = if (hgvsProteinImpact.startsWith("p.")) hgvsProteinImpact.substring(2) else hgvsProteinImpact
            if (impact.isEmpty()) {
                return impact
            }
            if (!MolecularInputChecker.isProteinImpact(impact)) {
                LOGGER.warn("Cannot convert hgvs protein impact to a usable protein impact: {}", hgvsProteinImpact)
            }
            return impact
        }
    }
}