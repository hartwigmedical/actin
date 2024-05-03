package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect

private const val PLOIDY_FACTOR = 3.0

private enum class CopyNumberEvaluation {
    REPORTABLE_FULL_AMP,
    REPORTABLE_PARTIAL_AMP,
    AMP_WITH_LOSS_OF_FUNCTION,
    AMP_ON_NON_ONCOGENE,
    UNREPORTABLE_AMP,
    NON_AMP_WITH_SUFFICIENT_COPY_NUMBER;

    companion object {
        fun fromCopyNumber(copyNumber: CopyNumber, ploidy: Double): CopyNumberEvaluation {
            val relativeMinCopies = copyNumber.minCopies / ploidy
            val relativeMaxCopies = copyNumber.maxCopies / ploidy

            return if (relativeMaxCopies >= PLOIDY_FACTOR) {
                when {
                    copyNumber.geneRole == GeneRole.TSG -> {
                        AMP_ON_NON_ONCOGENE
                    }

                    copyNumber.proteinEffect == ProteinEffect.LOSS_OF_FUNCTION
                            || copyNumber.proteinEffect == ProteinEffect.LOSS_OF_FUNCTION_PREDICTED -> {
                        AMP_WITH_LOSS_OF_FUNCTION
                    }

                    !copyNumber.isReportable -> {
                        UNREPORTABLE_AMP
                    }

                    relativeMinCopies < PLOIDY_FACTOR -> {
                        REPORTABLE_PARTIAL_AMP
                    }

                    else -> {
                        REPORTABLE_FULL_AMP
                    }
                }
            } else {
                NON_AMP_WITH_SUFFICIENT_COPY_NUMBER
            }
        }
    }
}

class GeneIsAmplified(private val gene: String, private val requestedMinCopyNumber: Int?) : MolecularEvaluationFunction {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val ploidy = molecular.characteristics.ploidy
            ?: return EvaluationFactory.fail(
                "Cannot determine amplification for gene $gene without ploidy", "Undetermined amplification for $gene"
            )

        val evaluatedCopyNumbers: Map<CopyNumberEvaluation, Set<String>> = molecular.drivers.copyNumbers.filter { copyNumber ->
            copyNumber.gene == gene && (requestedMinCopyNumber == null || copyNumber.minCopies >= requestedMinCopyNumber)
        }
            .groupBy({ copyNumber -> CopyNumberEvaluation.fromCopyNumber(copyNumber, ploidy) }, valueTransform = CopyNumber::event)
            .mapValues { (_, copyNumberEvents) -> copyNumberEvents.toSet() }

        val minCopyMessage = requestedMinCopyNumber?.let { " with >=$requestedMinCopyNumber copies" }
        return evaluatedCopyNumbers[CopyNumberEvaluation.REPORTABLE_FULL_AMP]?.let { reportableFullAmps ->
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
            ?: evaluatePotentialWarns(evaluatedCopyNumbers, molecular.evidenceSource)
            ?: EvaluationFactory.fail(
                "No amplification detected of gene $gene$minCopyMessage",
                if (requestedMinCopyNumber == null) "No amplification of $gene" else "Insufficient copies of $gene"
            )
    }

    private fun evaluatePotentialWarns(evaluatedCopyNumbers: Map<CopyNumberEvaluation, Set<String>>, evidenceSource: String): Evaluation? {
        val eventGroupsWithMessages = listOf(
            EventsWithMessages(
                evaluatedCopyNumbers[CopyNumberEvaluation.REPORTABLE_PARTIAL_AMP],
                "Gene $gene is partially amplified and not fully amplified",
                "$gene partially amplified"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[CopyNumberEvaluation.AMP_WITH_LOSS_OF_FUNCTION],
                "Gene $gene is amplified but event is annotated as having loss-of-function impact in $evidenceSource",
                "$gene amplification but gene associated with loss-of-function protein impact in $evidenceSource"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[CopyNumberEvaluation.AMP_ON_NON_ONCOGENE],
                "Gene $gene is amplified but gene $gene is known as TSG in $evidenceSource",
                "$gene amplification but $gene known as TSG in $evidenceSource"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[CopyNumberEvaluation.UNREPORTABLE_AMP],
                "Gene $gene is amplified but not considered reportable",
                "$gene amplification but considered not reportable"
            ),
            EventsWithMessages(
                requestedMinCopyNumber?.let { evaluatedCopyNumbers[CopyNumberEvaluation.NON_AMP_WITH_SUFFICIENT_COPY_NUMBER] },
                "Gene $gene does not meet cut-off for amplification, but has copy number > $requestedMinCopyNumber",
                "$gene has sufficient copies but not reported as amplification"
            )
        )

        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(eventGroupsWithMessages)
    }
}