package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect

class GeneIsAmplified(private val gene: String, private val requestedMinCopyNumber: Int? = null) : MolecularEvaluationFunction {

    private val copyNumberMessage = if (requestedMinCopyNumber != null) " with >$requestedMinCopyNumber copies" else ""

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val ploidy = molecular.characteristics.ploidy
            ?: return EvaluationFactory.fail(
                "Cannot determine amplification for gene $gene without ploidy", "Undetermined amplification for $gene"
            )
        val reportableFullAmps: MutableSet<String> = mutableSetOf()
        val reportablePartialAmps: MutableSet<String> = mutableSetOf()
        val ampsWithLossOfFunction: MutableSet<String> = mutableSetOf()
        val ampsOnNonOncogenes: MutableSet<String> = mutableSetOf()
        val ampsThatAreUnreportable: MutableSet<String> = mutableSetOf()
        val evidenceSource = molecular.evidenceSource

        for (copyNumber in molecular.drivers.copyNumbers) {
            if (copyNumber.gene == gene && copyNumber.minCopies >= (requestedMinCopyNumber ?: 0)) {
                val relativeMinCopies = copyNumber.minCopies / ploidy
                val relativeMaxCopies = copyNumber.maxCopies / ploidy
                val isAmplification = relativeMaxCopies >= PLOIDY_FACTOR
                val isNoOncogene = copyNumber.geneRole == GeneRole.TSG
                val isLossOfFunction = (copyNumber.proteinEffect == ProteinEffect.LOSS_OF_FUNCTION
                        || copyNumber.proteinEffect == ProteinEffect.LOSS_OF_FUNCTION_PREDICTED)
                if (isAmplification) {
                    if (isNoOncogene) {
                        ampsOnNonOncogenes.add(copyNumber.event)
                    } else if (isLossOfFunction) {
                        ampsWithLossOfFunction.add(copyNumber.event)
                    } else if (!copyNumber.isReportable) {
                        ampsThatAreUnreportable.add(copyNumber.event)
                    } else if (relativeMinCopies < PLOIDY_FACTOR) {
                        reportablePartialAmps.add(copyNumber.event)
                    } else {
                        reportableFullAmps.add(copyNumber.event)
                    }
                }
            }
        }
        if (reportableFullAmps.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Amplification detected of gene $gene$copyNumberMessage", "$gene is amplified$copyNumberMessage",
                inclusionEvents = reportableFullAmps
            )
        }
        val potentialWarnEvaluation = evaluatePotentialWarns(
            reportablePartialAmps,
            ampsWithLossOfFunction,
            ampsOnNonOncogenes,
            ampsThatAreUnreportable,
            evidenceSource
        )
        return potentialWarnEvaluation
            ?: EvaluationFactory.fail("No amplification detected of gene $gene$copyNumberMessage",
                "No amplification of $gene$copyNumberMessage"
            )
    }

    private fun evaluatePotentialWarns(
        reportablePartialAmps: Set<String>, ampsWithLossOfFunction: Set<String>,
        ampsOnNonOncogenes: Set<String>, ampsThatAreUnreportable: Set<String>, evidenceSource: String
    ): Evaluation? {
        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOf(
                EventsWithMessages(
                    reportablePartialAmps,
                    "Gene $gene is partially amplified$copyNumberMessage and not fully amplified",
                    "$gene partially amplified$copyNumberMessage"
                ),
                EventsWithMessages(
                    ampsWithLossOfFunction,
                    "Gene $gene is amplified$copyNumberMessage but event is annotated as having loss-of-function impact in $evidenceSource",
                    "$gene amplification$copyNumberMessage but gene associated with loss-of-function protein impact in $evidenceSource"
                ),
                EventsWithMessages(
                    ampsOnNonOncogenes,
                    "Gene $gene is amplified$copyNumberMessage but gene $gene is known as TSG in $evidenceSource",
                    "$gene amplification$copyNumberMessage but $gene known as TSG in $evidenceSource"
                ),
                EventsWithMessages(
                    ampsThatAreUnreportable,
                    "Gene $gene is amplified$copyNumberMessage but not considered reportable",
                    "$gene amplification$copyNumberMessage but considered not reportable"
                )
            )
        )
    }

    companion object {
        private const val PLOIDY_FACTOR = 3.0
    }
}