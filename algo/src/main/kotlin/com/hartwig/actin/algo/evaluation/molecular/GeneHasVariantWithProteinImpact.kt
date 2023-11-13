package com.hartwig.actin.algo.evaluation.molecular

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Sets
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.Format.percentage
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import org.apache.logging.log4j.LogManager

class GeneHasVariantWithProteinImpact internal constructor(private val gene: String, private val allowedProteinImpacts: List<String>) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val canonicalReportableVariantMatches: MutableSet<String> = Sets.newHashSet()
        val canonicalReportableSubclonalVariantMatches: MutableSet<String> = Sets.newHashSet()
        val canonicalUnreportableVariantMatches: MutableSet<String> = Sets.newHashSet()
        val canonicalProteinImpactMatches: MutableSet<String> = Sets.newHashSet()
        val reportableOtherVariantMatches: MutableSet<String> = Sets.newHashSet()
        val reportableOtherProteinImpactMatches: MutableSet<String> = Sets.newHashSet()
        for (variant in record.molecular().drivers().variants()) {
            if (variant.gene() == gene) {
                val canonicalProteinImpact = toProteinImpact(variant.canonicalImpact().hgvsProteinImpact())
                for (allowedProteinImpact in allowedProteinImpacts) {
                    if (canonicalProteinImpact == allowedProteinImpact) {
                        canonicalProteinImpactMatches.add(allowedProteinImpact)
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
                        for (otherProteinImpact in toProteinImpacts(variant.otherImpacts())) {
                            if (otherProteinImpact == allowedProteinImpact) {
                                reportableOtherVariantMatches.add(variant.event())
                                reportableOtherProteinImpactMatches.add(allowedProteinImpact)
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
                    "Variant(s) " + concat(canonicalProteinImpactMatches) + " in gene " + gene
                            + " detected in canonical transcript"
                )
                .addPassGeneralMessages(concat(canonicalProteinImpactMatches) + " found in " + gene)
                .build()
        }
        val potentialWarnEvaluation = evaluatePotentialWarns(
            canonicalReportableSubclonalVariantMatches,
            canonicalUnreportableVariantMatches,
            canonicalProteinImpactMatches,
            reportableOtherVariantMatches,
            reportableOtherProteinImpactMatches
        )
        return potentialWarnEvaluation
            ?: unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("None of " + concat(allowedProteinImpacts) + " detected in gene " + gene)
                .addFailGeneralMessages(concat(allowedProteinImpacts) + " not detected in $gene")
                .build()
    }

    private fun evaluatePotentialWarns(
        canonicalReportableSubclonalVariantMatches: Set<String>,
        canonicalUnreportableVariantMatches: Set<String>, canonicalProteinImpactMatches: Set<String>,
        reportableOtherVariantMatches: Set<String>, reportableOtherProteinImpactMatches: Set<String>
    ): Evaluation? {
        val warnEvents: MutableSet<String> = Sets.newHashSet()
        val warnSpecificMessages: MutableSet<String> = Sets.newHashSet()
        val warnGeneralMessages: MutableSet<String> = Sets.newHashSet()
        if (canonicalReportableSubclonalVariantMatches.isNotEmpty()) {
            warnEvents.addAll(canonicalReportableSubclonalVariantMatches)
            warnSpecificMessages.add(
                "Variant(s) " + concat(canonicalReportableSubclonalVariantMatches) + " in " + gene
                        + " detected in canonical transcript but subclonal likelihood of > " + percentage(1 - CLONAL_CUTOFF)
            )
            warnGeneralMessages.add(
                "Variant(s) " + concat(canonicalReportableSubclonalVariantMatches) + " in " + gene
                        + " but subclonal likelihood of > " + percentage(1 - CLONAL_CUTOFF)
            )
        }
        if (canonicalUnreportableVariantMatches.isNotEmpty()) {
            warnEvents.addAll(canonicalUnreportableVariantMatches)
            warnSpecificMessages.add(
                "Variant(s) " + concat(canonicalProteinImpactMatches) + " in " + gene
                        + " detected in canonical transcript but are not reportable"
            )
            warnGeneralMessages.add(concat(canonicalProteinImpactMatches) + " found in " + gene)
        }
        if (reportableOtherVariantMatches.isNotEmpty()) {
            warnEvents.addAll(reportableOtherVariantMatches)
            warnSpecificMessages.add(
                "Variant(s) " + concat(reportableOtherProteinImpactMatches) + " in " + gene
                        + " detected but in non-canonical transcript"
            )
            warnGeneralMessages.add(
                concat(reportableOtherProteinImpactMatches) + " found in non-canonical transcript of gene " + gene
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
        private val LOGGER = LogManager.getLogger(
            GeneHasVariantWithProteinImpact::class.java
        )
        private const val CLONAL_CUTOFF = 0.5
        private fun toProteinImpacts(impacts: Set<TranscriptImpact>): Set<String> {
            val proteinImpacts: MutableSet<String> = Sets.newHashSet()
            for (impact in impacts) {
                proteinImpacts.add(toProteinImpact(impact.hgvsProteinImpact()))
            }
            return proteinImpacts
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