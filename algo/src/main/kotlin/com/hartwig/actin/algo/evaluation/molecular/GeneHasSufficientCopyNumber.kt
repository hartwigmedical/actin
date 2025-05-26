package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import java.time.LocalDate

private const val ASSUMED_MIN_COPY_NR_AMP = 6

private enum class CopyNumberEvaluation {
    ELIGIBLE_MIN_COPY_NUMBER,
    SUFFICIENT_MIN_COPY_NUMBER_ON_NON_ONCOGENE,
    SUFFICIENT_MIN_COPY_NUMBER_WITH_LOSS_OF_FUNCTION,
    SUFFICIENT_MAX_COPY_NUMBER,
    SUFFICIENT_MIN_COPY_NUMBER_ON_NON_CANONICAL,
    AMP_WITH_UNKNOWN_MIN_COPY_NUMBER,
    INELIGIBLE_COPY_NUMBER;

    companion object {
        fun fromCopyNumber(copyNumber: CopyNumber, requestedMinCopyNumber: Int): CopyNumberEvaluation {

            return when {
                copyNumber.canonicalImpact.minCopies?.let { it >= requestedMinCopyNumber } == true -> {
                    when {
                        copyNumber.geneRole == GeneRole.TSG -> SUFFICIENT_MIN_COPY_NUMBER_ON_NON_ONCOGENE

                        copyNumber.proteinEffect == ProteinEffect.LOSS_OF_FUNCTION ||
                                copyNumber.proteinEffect == ProteinEffect.LOSS_OF_FUNCTION_PREDICTED -> SUFFICIENT_MIN_COPY_NUMBER_WITH_LOSS_OF_FUNCTION

                        else -> ELIGIBLE_MIN_COPY_NUMBER
                    }
                }

                (copyNumber.canonicalImpact.type.isGain || copyNumber.otherImpacts.any { it.type.isGain }) &&
                        copyNumber.canonicalImpact.minCopies == null && copyNumber.otherImpacts.all { it.minCopies == null } -> AMP_WITH_UNKNOWN_MIN_COPY_NUMBER

                copyNumber.canonicalImpact.maxCopies?.let { it >= requestedMinCopyNumber } == true -> SUFFICIENT_MAX_COPY_NUMBER

                copyNumber.otherImpacts.any { it -> it.minCopies?.let { it >= requestedMinCopyNumber } == true } -> SUFFICIENT_MIN_COPY_NUMBER_ON_NON_CANONICAL

                else -> INELIGIBLE_COPY_NUMBER
            }
        }
    }
}

class GeneHasSufficientCopyNumber(override val gene: String, private val requestedMinCopyNumber: Int, maxTestAge: LocalDate? = null) :
    MolecularEvaluationFunction(
        targetCoveragePredicate = or(
            MolecularTestTarget.AMPLIFICATION,
            MolecularTestTarget.MUTATION,
            messagePrefix = "Sufficient copy number in"
        ),
        maxTestAge = maxTestAge
    ) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val targetCopyNumbers = test.drivers.copyNumbers.filter { it.gene == gene }
        val evaluatedCopyNumbers: Map<CopyNumberEvaluation, Set<String>> = targetCopyNumbers
            .groupingBy { CopyNumberEvaluation.fromCopyNumber(it, requestedMinCopyNumber) }
            .fold(emptySet()) { acc, copyNumber -> acc + copyNumber.event }

        val reportableSufficientCN = evaluatedCopyNumbers[CopyNumberEvaluation.ELIGIBLE_MIN_COPY_NUMBER]
        val amplifiedWithUnknownCN = evaluatedCopyNumbers[CopyNumberEvaluation.AMP_WITH_UNKNOWN_MIN_COPY_NUMBER]

        return when {
            reportableSufficientCN != null -> {
                EvaluationFactory.pass(
                    "$gene copy number is above $requestedMinCopyNumber",
                    inclusionEvents = reportableSufficientCN
                )
            }

            amplifiedWithUnknownCN != null -> {
                if (requestedMinCopyNumber <= ASSUMED_MIN_COPY_NR_AMP)
                    EvaluationFactory.pass(
                        "$gene is amplified hence assumed gene has a copy number >$requestedMinCopyNumber copies",
                        inclusionEvents = amplifiedWithUnknownCN
                    ) else
                    EvaluationFactory.warn(
                        "$gene is amplified but undetermined if gene has a copy number >$requestedMinCopyNumber copies",
                        inclusionEvents = amplifiedWithUnknownCN
                    )
            }

            else -> evaluatePotentialWarns(evaluatedCopyNumbers, test.evidenceSource)
                ?: EvaluationFactory.fail("$gene does not have at least $requestedMinCopyNumber copies")
        }
    }

    private fun evaluatePotentialWarns(
        evaluatedCopyNumbers: Map<CopyNumberEvaluation, Set<String>>,
        evidenceSource: String
    ): Evaluation? {
        val eventGroupsWithMessages = listOf(
            EventsWithMessages(
                evaluatedCopyNumbers[CopyNumberEvaluation.SUFFICIENT_MIN_COPY_NUMBER_WITH_LOSS_OF_FUNCTION],
                "$gene has sufficient copies but gene associated with loss-of-function protein impact in $evidenceSource"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[CopyNumberEvaluation.SUFFICIENT_MIN_COPY_NUMBER_ON_NON_ONCOGENE],
                "$gene has sufficient copies but gene known as TSG in $evidenceSource"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[CopyNumberEvaluation.SUFFICIENT_MIN_COPY_NUMBER_ON_NON_CANONICAL],
                "$gene has sufficient copies but only on non-canonical transcript"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[CopyNumberEvaluation.SUFFICIENT_MAX_COPY_NUMBER],
                "$gene has sufficient copies but only partially"
            )
        )

        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(eventGroupsWithMessages)
    }
}