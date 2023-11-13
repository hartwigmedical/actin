package com.hartwig.actin.algo.evaluation.molecular

import com.google.common.collect.Sets
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect

//TODO: Merge with GeneIsAmplified
class GeneIsAmplifiedMinCopies(private val gene: String, private val requestedMinCopyNumber: Int) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val ploidy = record.molecular().characteristics().ploidy()
            ?: return unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Cannot determine amplification for gene $gene without ploidy")
                .addFailGeneralMessages("Undetermined amplification for $gene")
                .build()
        val reportableFullAmps: MutableSet<String> = Sets.newHashSet()
        val reportablePartialAmps: MutableSet<String> = Sets.newHashSet()
        val ampsWithLossOfFunction: MutableSet<String> = Sets.newHashSet()
        val ampsOnNonOncogenes: MutableSet<String> = Sets.newHashSet()
        val ampsThatAreUnreportable: MutableSet<String> = Sets.newHashSet()
        val ampsThatAreNearCutoff: MutableSet<String> = Sets.newHashSet()
        val nonAmpsWithSufficientCopyNumber: MutableSet<String> = Sets.newHashSet()
        for (copyNumber in record.molecular().drivers().copyNumbers()) {
            if (copyNumber.gene() == gene && copyNumber.minCopies() >= requestedMinCopyNumber) {
                val relativeMinCopies = copyNumber.minCopies() / ploidy
                val relativeMaxCopies = copyNumber.maxCopies() / ploidy
                val isAmplification = relativeMaxCopies >= HARD_PLOIDY_FACTOR
                val isNearAmp = relativeMinCopies >= SOFT_PLOIDY_FACTOR && relativeMaxCopies <= HARD_PLOIDY_FACTOR
                val isNoOncogene = copyNumber.geneRole() == GeneRole.TSG
                val isLossOfFunction = (copyNumber.proteinEffect() == ProteinEffect.LOSS_OF_FUNCTION
                        || copyNumber.proteinEffect() == ProteinEffect.LOSS_OF_FUNCTION_PREDICTED)
                if (isAmplification) {
                    if (isNoOncogene) {
                        ampsOnNonOncogenes.add(copyNumber.event())
                    } else if (isLossOfFunction) {
                        ampsWithLossOfFunction.add(copyNumber.event())
                    } else if (!copyNumber.isReportable) {
                        ampsThatAreUnreportable.add(copyNumber.event())
                    } else if (relativeMinCopies < HARD_PLOIDY_FACTOR) {
                        reportablePartialAmps.add(copyNumber.event())
                    } else {
                        reportableFullAmps.add(copyNumber.event())
                    }
                } else if (isNearAmp) {
                    ampsThatAreNearCutoff.add(copyNumber.event())
                } else {
                    nonAmpsWithSufficientCopyNumber.add(copyNumber.event())
                }
            }
        }
        if (reportableFullAmps.isNotEmpty()) {
            return unrecoverable()
                .result(EvaluationResult.PASS)
                .addAllInclusionMolecularEvents(reportableFullAmps)
                .addPassSpecificMessages(
                    "Amplification detected of gene $gene with min copy number of $requestedMinCopyNumber"
                )
                .addPassGeneralMessages("$gene is amplified with >$requestedMinCopyNumber copies")
                .build()
        }
        val potentialWarnEvaluation = evaluatePotentialWarns(
            reportablePartialAmps,
            ampsWithLossOfFunction,
            ampsOnNonOncogenes,
            ampsThatAreUnreportable,
            ampsThatAreNearCutoff,
            nonAmpsWithSufficientCopyNumber
        )
        return potentialWarnEvaluation
            ?: unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No amplification detected of gene $gene with min copy number of $requestedMinCopyNumber")
                .addFailGeneralMessages("No sufficient copies of $gene")
                .build()
    }

    private fun evaluatePotentialWarns(
        reportablePartialAmps: Set<String>, ampsWithLossOfFunction: Set<String>,
        ampsOnNonOncogenes: Set<String>, ampsThatAreUnreportable: Set<String>,
        ampsThatAreNearCutoff: Set<String>, nonAmpsWithSufficientCopyNumber: Set<String>
    ): Evaluation? {
        val warnEvents: MutableSet<String> = Sets.newHashSet()
        val warnSpecificMessages: MutableSet<String> = Sets.newHashSet()
        val warnGeneralMessages: MutableSet<String> = Sets.newHashSet()
        if (reportablePartialAmps.isNotEmpty()) {
            warnEvents.addAll(reportablePartialAmps)
            warnSpecificMessages.add("Gene $gene is partially amplified and not fully amplified")
            warnGeneralMessages.add("$gene partially amplified")
        }
        if (ampsWithLossOfFunction.isNotEmpty()) {
            warnEvents.addAll(ampsWithLossOfFunction)
            warnSpecificMessages.add("Gene $gene is amplified but event is annotated as having loss-of-function impact")
            warnGeneralMessages.add("$gene amplification but with loss-of-function protein impact")
        }
        if (ampsOnNonOncogenes.isNotEmpty()) {
            warnEvents.addAll(ampsOnNonOncogenes)
            warnSpecificMessages.add("Gene $gene is amplified but gene $gene is known as TSG")
            warnGeneralMessages.add("$gene amplification but $gene known as TSG")
        }
        if (ampsThatAreUnreportable.isNotEmpty()) {
            warnEvents.addAll(ampsThatAreUnreportable)
            warnSpecificMessages.add("Gene $gene is amplified but not considered reportable")
            warnGeneralMessages.add("$gene amplification but considered not reportable")
        }
        if (ampsThatAreNearCutoff.isNotEmpty()) {
            warnEvents.addAll(ampsThatAreNearCutoff)
            warnSpecificMessages.add("Gene $gene does not meet cut-off for amplification, but is near cut-off")
            warnGeneralMessages.add("$gene near cut-off for amplification")
        }
        if (nonAmpsWithSufficientCopyNumber.isNotEmpty()) {
            warnEvents.addAll(ampsThatAreNearCutoff)
            warnSpecificMessages.add(
                "Gene $gene does not meet cut-off for amplification, but has copy number > $requestedMinCopyNumber"
            )
            warnGeneralMessages.add("$gene has sufficient copies but not reported as amplification")
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
        private const val SOFT_PLOIDY_FACTOR = 2.5
        private const val HARD_PLOIDY_FACTOR = 3.0
    }
}