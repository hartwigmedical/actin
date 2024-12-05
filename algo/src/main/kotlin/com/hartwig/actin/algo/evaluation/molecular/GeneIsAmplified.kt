package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.GeneRole
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import java.time.LocalDate

private const val PLOIDY_FACTOR = 3.0

private enum class CopyNumberEvaluation {
    REPORTABLE_FULL_AMP,
    REPORTABLE_PARTIAL_AMP,
    AMP_WITH_LOSS_OF_FUNCTION,
    AMP_ON_NON_ONCOGENE,
    UNREPORTABLE_AMP,
    NON_AMP_WITH_SUFFICIENT_COPY_NUMBER;

    companion object {
        fun fromCopyNumber(copyNumber: CopyNumber, relativeMinCopies: Double, relativeMaxCopies: Double): CopyNumberEvaluation {
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

class GeneIsAmplified(private val gene: String, private val requestedMinCopyNumber: Int?, maxTestAge: LocalDate? = null) :
    MolecularEvaluationFunction(maxTestAge) {

    override fun genes() = listOf(gene)

    override fun evaluate(test: MolecularTest): Evaluation {
        val ploidy = test.characteristics.ploidy
            ?: return EvaluationFactory.fail(
                "Cannot determine amplification for gene $gene without ploidy", "Undetermined amplification for $gene"
            )

        val copyNumbersForGene = test.drivers.copyNumbers.filter { copyNumber -> copyNumber.gene == gene }

        val evaluatedCanonicalCopyNumbers: Map<CopyNumberEvaluation, Set<String>> = copyNumbersForGene.filter {
            (requestedMinCopyNumber == null || it.canonicalImpact.minCopies >= requestedMinCopyNumber)
        }
            .groupBy({ copyNumber ->
                CopyNumberEvaluation.fromCopyNumber(
                    copyNumber,
                    copyNumber.canonicalImpact.minCopies / ploidy,
                    copyNumber.canonicalImpact.maxCopies / ploidy
                )
            }, valueTransform = CopyNumber::event)
            .mapValues { (_, copyNumberEvents) -> copyNumberEvents.toSet() }

        val evaluatedOtherImpactsCopyNumbers: Map<CopyNumberEvaluation, Set<String>> = copyNumbersForGene.filter { copyNumber ->
            (requestedMinCopyNumber == null || copyNumber.otherImpacts.any { it.minCopies >= requestedMinCopyNumber })
        }
            .groupBy({ copyNumber ->
                copyNumber.otherImpacts.map { otherImpact ->
                    CopyNumberEvaluation.fromCopyNumber(
                        copyNumber,
                        otherImpact.minCopies / ploidy,
                        otherImpact.maxCopies / ploidy
                    )
                }
            }, valueTransform = CopyNumber::event)
            .flatMap { (evaluations, copyNumberEvents) -> evaluations.map { it to copyNumberEvents.toSet() } }
            .toMap()

        val minCopyMessage = requestedMinCopyNumber?.let { " with >=$requestedMinCopyNumber copies" } ?: ""
        val nonCanonicalMessage = if (evaluatedCanonicalCopyNumbers.isNotEmpty()) "" else " on non-canonical transcript"
        return if (evaluatedCanonicalCopyNumbers.isNotEmpty()) {
            evaluateAmplification(evaluatedCanonicalCopyNumbers, minCopyMessage, nonCanonicalMessage, test.evidenceSource)
        } else {
            evaluateAmplification(
                evaluatedOtherImpactsCopyNumbers,
                minCopyMessage,
                nonCanonicalMessage,
                test.evidenceSource
            )
        }
    }

    private fun evaluateAmplification(
        evaluatedCopyNumbers: Map<CopyNumberEvaluation, Set<String>>,
        minCopyMessage: String,
        nonCanonicalMessage: String,
        evidenceSource: String
    ): Evaluation {
        return evaluatedCopyNumbers[CopyNumberEvaluation.REPORTABLE_FULL_AMP]?.let { reportableFullAmps ->
            EvaluationFactory.pass(
                "Amplification detected of gene $gene$minCopyMessage$nonCanonicalMessage",
                "$gene is amplified$minCopyMessage$nonCanonicalMessage",
                inclusionEvents = reportableFullAmps
            )
        }
            ?: requestedMinCopyNumber?.let { evaluatedCopyNumbers[CopyNumberEvaluation.UNREPORTABLE_AMP] }
                ?.let { ampsThatAreUnreportable ->
                    EvaluationFactory.pass(
                        "Gene $gene has a copy number that exceeds the threshold of $requestedMinCopyNumber copies but is considered not reportable$nonCanonicalMessage",
                        "$gene has a copy number >$requestedMinCopyNumber copies$nonCanonicalMessage",
                        inclusionEvents = ampsThatAreUnreportable
                    )
                }
            ?: evaluatePotentialWarns(evaluatedCopyNumbers, evidenceSource, nonCanonicalMessage)
            ?: EvaluationFactory.fail(
                "No amplification detected of gene $gene$minCopyMessage",
                if (requestedMinCopyNumber == null) "No amplification of $gene" else "Insufficient copies of $gene"
            )
    }

    private fun evaluatePotentialWarns(
        evaluatedCopyNumbers: Map<CopyNumberEvaluation, Set<String>>,
        evidenceSource: String,
        nonCanonicalMessage: String
    ): Evaluation? {
        val eventGroupsWithMessages = listOf(
            EventsWithMessages(
                evaluatedCopyNumbers[CopyNumberEvaluation.REPORTABLE_PARTIAL_AMP],
                "Gene $gene is partially amplified and not fully amplified$nonCanonicalMessage",
                "$gene partially amplified$nonCanonicalMessage"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[CopyNumberEvaluation.AMP_WITH_LOSS_OF_FUNCTION],
                "Gene $gene is amplified but event is annotated as having loss-of-function impact in $evidenceSource$nonCanonicalMessage",
                "$gene amplification but gene associated with loss-of-function protein impact in $evidenceSource$nonCanonicalMessage"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[CopyNumberEvaluation.AMP_ON_NON_ONCOGENE],
                "Gene $gene is amplified but gene $gene is known as TSG in $evidenceSource$nonCanonicalMessage",
                "$gene amplification but $gene known as TSG in $evidenceSource$nonCanonicalMessage"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[CopyNumberEvaluation.UNREPORTABLE_AMP],
                "Gene $gene is amplified but not considered reportable$nonCanonicalMessage",
                "$gene amplification but considered not reportable$nonCanonicalMessage"
            ),
            EventsWithMessages(
                requestedMinCopyNumber?.let { evaluatedCopyNumbers[CopyNumberEvaluation.NON_AMP_WITH_SUFFICIENT_COPY_NUMBER] },
                "Gene $gene does not meet cut-off for amplification, but has copy number > $requestedMinCopyNumber$nonCanonicalMessage",
                "$gene has sufficient copies but not reported as amplification$nonCanonicalMessage"
            )
        )

        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(eventGroupsWithMessages)
    }
}