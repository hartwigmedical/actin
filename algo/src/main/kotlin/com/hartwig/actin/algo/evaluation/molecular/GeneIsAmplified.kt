package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect

private enum class CopyNumberEvaluation {
    REPORTABLE_FULL_AMP,
    REPORTABLE_PARTIAL_AMP,
    AMP_WITH_LOSS_OF_FUNCTION,
    AMP_ON_NON_ONCOGENE,
    UNREPORTABLE_AMP,
    AMP_NEAR_CUTOFF,
    NON_AMP_WITH_SUFFICIENT_COPY_NUMBER
}

class GeneIsAmplified(private val gene: String, private val requestedMinCopyNumber: Int?) : MolecularEvaluationFunction {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val ploidy = molecular.characteristics.ploidy
            ?: return EvaluationFactory.fail(
                "Cannot determine amplification for gene $gene without ploidy", "Undetermined amplification for $gene"
            )

        val evaluatedCopyNumbers = molecular.drivers.copyNumbers.filter { copyNumber ->
            copyNumber.gene == gene && (requestedMinCopyNumber == null || copyNumber.minCopies >= requestedMinCopyNumber)
        }
            .groupBy(
                { copyNumber ->
                    val relativeMinCopies = copyNumber.minCopies / ploidy
                    val relativeMaxCopies = copyNumber.maxCopies / ploidy

                    if (relativeMaxCopies >= HARD_PLOIDY_FACTOR) {
                        when {
                            copyNumber.geneRole == GeneRole.TSG -> {
                                CopyNumberEvaluation.AMP_ON_NON_ONCOGENE
                            }

                            copyNumber.proteinEffect == ProteinEffect.LOSS_OF_FUNCTION
                                    || copyNumber.proteinEffect == ProteinEffect.LOSS_OF_FUNCTION_PREDICTED -> {
                                CopyNumberEvaluation.AMP_WITH_LOSS_OF_FUNCTION
                            }

                            !copyNumber.isReportable -> {
                                CopyNumberEvaluation.UNREPORTABLE_AMP
                            }

                            relativeMinCopies < HARD_PLOIDY_FACTOR -> {
                                CopyNumberEvaluation.REPORTABLE_PARTIAL_AMP
                            }

                            else -> {
                                CopyNumberEvaluation.REPORTABLE_FULL_AMP
                            }
                        }
                    } else if (relativeMinCopies >= SOFT_PLOIDY_FACTOR && relativeMaxCopies < HARD_PLOIDY_FACTOR) {
                        CopyNumberEvaluation.AMP_NEAR_CUTOFF
                    } else {
                        CopyNumberEvaluation.NON_AMP_WITH_SUFFICIENT_COPY_NUMBER
                    }
                },
                CopyNumber::event
            )
            .mapValues { (_, copyNumbers) -> copyNumbers.toSet() }

        return evaluatedCopyNumbers[CopyNumberEvaluation.REPORTABLE_FULL_AMP]?.let { reportableFullAmps ->
            val minCopyMessage = requestedMinCopyNumber?.let { " with >=$requestedMinCopyNumber copies" }
            EvaluationFactory.pass(
                "Amplification detected of gene $gene$minCopyMessage",
                "$gene is amplified$minCopyMessage",
                inclusionEvents = reportableFullAmps
            )
        }
            ?: requestedMinCopyNumber?.let { evaluatedCopyNumbers[CopyNumberEvaluation.UNREPORTABLE_AMP] }?.let { ampsThatAreUnreportable ->
                EvaluationFactory.pass(
                    "Gene $gene has a copy number that exceeds the threshold of $requestedMinCopyNumber copies but is considered not reportable",
                    "$gene has a copy number >$requestedMinCopyNumber copies",
                    inclusionEvents = ampsThatAreUnreportable
                )
            }
            ?: evaluatePotentialWarns(
                evaluatedCopyNumbers[CopyNumberEvaluation.REPORTABLE_PARTIAL_AMP],
                evaluatedCopyNumbers[CopyNumberEvaluation.AMP_WITH_LOSS_OF_FUNCTION],
                evaluatedCopyNumbers[CopyNumberEvaluation.AMP_ON_NON_ONCOGENE],
                evaluatedCopyNumbers[CopyNumberEvaluation.UNREPORTABLE_AMP],
                evaluatedCopyNumbers[CopyNumberEvaluation.AMP_NEAR_CUTOFF],
                requestedMinCopyNumber?.let { evaluatedCopyNumbers[CopyNumberEvaluation.NON_AMP_WITH_SUFFICIENT_COPY_NUMBER] },
                molecular.evidenceSource
            )
            ?: EvaluationFactory.fail(
                "No amplification detected of gene $gene with min copy number of $requestedMinCopyNumber", "No sufficient copies of $gene"
            )
    }

    private fun evaluatePotentialWarns(
        reportablePartialAmps: Set<String>?,
        ampsWithLossOfFunction: Set<String>?,
        ampsOnNonOncogenes: Set<String>?,
        unreportableAmps: Set<String>?,
        ampsThatAreNearCutoff: Set<String>?,
        nonAmpsWithSufficientCopyNumber: Set<String>?,
        evidenceSource: String
    ): Evaluation? {
        val eventGroupsWithMessages = listOf(
            Triple(
                reportablePartialAmps,
                "Gene $gene is partially amplified and not fully amplified",
                "$gene partially amplified"
            ),
            Triple(
                ampsWithLossOfFunction,
                "Gene $gene is amplified but event is annotated as having loss-of-function impact in $evidenceSource",
                "$gene amplification but gene associated with loss-of-function protein impact in $evidenceSource"
            ),
            Triple(
                ampsOnNonOncogenes,
                "Gene $gene is amplified but gene $gene is known as TSG in $evidenceSource",
                "$gene amplification but $gene known as TSG in $evidenceSource"
            ),
            Triple(
                unreportableAmps,
                "Gene $gene is amplified but not considered reportable",
                "$gene amplification but considered not reportable"
            ),
            Triple(
                ampsThatAreNearCutoff,
                "Gene $gene does not meet cut-off for amplification, but is near cut-off",
                "$gene near cut-off for amplification"
            ),
            Triple(
                nonAmpsWithSufficientCopyNumber,
                "Gene $gene does not meet cut-off for amplification, but has copy number > $requestedMinCopyNumber",
                "$gene has sufficient copies but not reported as amplification"
            )
        )
            .map { (events, specificMessage, generalMessage) ->
                EventsWithMessages(events ?: emptySet(), specificMessage, generalMessage)
            }

        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(eventGroupsWithMessages)
    }

    companion object {
        private const val SOFT_PLOIDY_FACTOR = 2.5
        private const val HARD_PLOIDY_FACTOR = 3.0
    }
}