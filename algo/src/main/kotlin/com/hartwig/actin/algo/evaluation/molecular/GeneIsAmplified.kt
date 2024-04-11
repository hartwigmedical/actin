package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect

class GeneIsAmplified(private val gene: String) : MolecularEvaluationFunction {

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
        val ampsThatAreNearCutoff: MutableSet<String> = mutableSetOf()
        val evidenceSource = molecular.evidenceSource

        for (copyNumber in molecular.drivers.copyNumbers) {
            if (copyNumber.gene == gene) {
                val relativeMinCopies = copyNumber.minCopies / ploidy
                val relativeMaxCopies = copyNumber.maxCopies / ploidy
                val isAmplification = relativeMaxCopies >= HARD_PLOIDY_FACTOR
                val isNearAmp = relativeMinCopies >= SOFT_PLOIDY_FACTOR && relativeMaxCopies <= HARD_PLOIDY_FACTOR
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
                    } else if (relativeMinCopies < HARD_PLOIDY_FACTOR) {
                        reportablePartialAmps.add(copyNumber.event)
                    } else {
                        reportableFullAmps.add(copyNumber.event)
                    }
                } else if (isNearAmp) {
                    ampsThatAreNearCutoff.add(copyNumber.event)
                }
            }
        }
        if (reportableFullAmps.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Amplification detected of gene $gene", "$gene is amplified", inclusionEvents = reportableFullAmps
            )
        }
        val potentialWarnEvaluation = evaluatePotentialWarns(
            reportablePartialAmps,
            ampsWithLossOfFunction,
            ampsOnNonOncogenes,
            ampsThatAreUnreportable,
            ampsThatAreNearCutoff,
            evidenceSource
        )
        return potentialWarnEvaluation
            ?: EvaluationFactory.fail("No amplification detected of gene $gene", "No amplification of $gene")
    }

    private fun evaluatePotentialWarns(
        reportablePartialAmps: Set<String>, ampsWithLossOfFunction: Set<String>,
        ampsOnNonOncogenes: Set<String>, ampsThatAreUnreportable: Set<String>,
        ampsThatAreNearCutoff: Set<String>, evidenceSource: String
    ): Evaluation? {
        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOf(
                EventsWithMessages(
                    reportablePartialAmps,
                    "Gene $gene is partially amplified and not fully amplified",
                    "$gene partially amplified"
                ),
                EventsWithMessages(
                    ampsWithLossOfFunction,
                    "Gene $gene is amplified but event is annotated as having loss-of-function impact in $evidenceSource",
                    "$gene amplification but gene associated with loss-of-function protein impact in $evidenceSource"
                ),
                EventsWithMessages(
                    ampsOnNonOncogenes,
                    "Gene $gene is amplified but gene $gene is known as TSG in $evidenceSource",
                    "$gene amplification but $gene known as TSG in $evidenceSource"
                ),
                EventsWithMessages(
                    ampsThatAreUnreportable,
                    "Gene $gene is amplified but not considered reportable",
                    "$gene amplification but considered not reportable"
                ),
                EventsWithMessages(
                    ampsThatAreNearCutoff,
                    "Gene $gene does not meet cut-off for amplification but is near cut-off",
                    "$gene near cut-off for amplification"
                )
            )
        )
    }

    companion object {
        private const val SOFT_PLOIDY_FACTOR = 2.5
        private const val HARD_PLOIDY_FACTOR = 3.0
    }
}