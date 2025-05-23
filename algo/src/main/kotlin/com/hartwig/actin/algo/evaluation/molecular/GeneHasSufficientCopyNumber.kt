package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import java.time.LocalDate

private const val ASSUMED_MIN_COPY_NR_AMP = 6

private enum class CopyNumberEvaluation {
    AMP_WITH_UNKNOWN_COPY_NUMBER,
    REPORTABLE_SUFFICIENT_COPY_NUMBER,
    SUFFICIENT_COPY_NUMBER_WITH_LOSS_OF_FUNCTION,
    SUFFICIENT_COPY_NUMBER_ON_NON_ONCOGENE,
    UNREPORTABLE_SUFFICIENT_COPY_NUMBER,
    INSUFFICIENT_COPY_NUMBER;

    companion object {
        fun fromCopyNumber(copyNumber: CopyNumber, requestedMinCopyNumber: Int): CopyNumberEvaluation {
            val copyNumberIsAmp = copyNumber.canonicalImpact.type in setOf(
                CopyNumberType.FULL_GAIN,
                CopyNumberType.PARTIAL_GAIN
            ) || copyNumber.otherImpacts.any { it.type in setOf(CopyNumberType.FULL_GAIN, CopyNumberType.PARTIAL_GAIN) }
            val copyNumberHasUnknownCopies =
                copyNumber.canonicalImpact.minCopies == null && copyNumber.otherImpacts.none { it.minCopies != null }

            return if (copyNumberIsAmp && copyNumberHasUnknownCopies) {
                AMP_WITH_UNKNOWN_COPY_NUMBER
            } else if ((copyNumber.canonicalImpact.minCopies?.let { it >= requestedMinCopyNumber }) == true) {
                when {
                    copyNumber.geneRole == GeneRole.TSG -> {
                        SUFFICIENT_COPY_NUMBER_ON_NON_ONCOGENE
                    }

                    copyNumber.proteinEffect == ProteinEffect.LOSS_OF_FUNCTION
                            || copyNumber.proteinEffect == ProteinEffect.LOSS_OF_FUNCTION_PREDICTED -> {
                        SUFFICIENT_COPY_NUMBER_WITH_LOSS_OF_FUNCTION
                    }

                    !copyNumber.isReportable -> {
                        UNREPORTABLE_SUFFICIENT_COPY_NUMBER
                    }

                    else -> {
                        REPORTABLE_SUFFICIENT_COPY_NUMBER
                    }
                }
            } else {
                INSUFFICIENT_COPY_NUMBER
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

        val eventsOnNonCanonical =
            targetCopyNumbers.filter { copyNumber -> copyNumber.otherImpacts.any { it.minCopies != null && it.minCopies!! >= requestedMinCopyNumber } }
                .map { it.event }.toSet()
        val reportableSufficientCN = evaluatedCopyNumbers[CopyNumberEvaluation.REPORTABLE_SUFFICIENT_COPY_NUMBER]
        val unreportableSufficientCN = evaluatedCopyNumbers[CopyNumberEvaluation.UNREPORTABLE_SUFFICIENT_COPY_NUMBER]
        val amplifiedWithUnknownCN = evaluatedCopyNumbers[CopyNumberEvaluation.AMP_WITH_UNKNOWN_COPY_NUMBER]

        return when {
            reportableSufficientCN != null -> {
                EvaluationFactory.pass(
                    "$gene copy number is above $requestedMinCopyNumber",
                    inclusionEvents = reportableSufficientCN
                )
            }

            unreportableSufficientCN != null -> {
                EvaluationFactory.pass(
                    "$gene has a copy number >$requestedMinCopyNumber copies but is considered not reportable",
                    inclusionEvents = unreportableSufficientCN
                )
            }

            amplifiedWithUnknownCN != null -> {
                if (requestedMinCopyNumber <= ASSUMED_MIN_COPY_NR_AMP)
                    EvaluationFactory.pass(
                        "$gene is amplified hence assumed gene has a copy number >$requestedMinCopyNumber",
                        inclusionEvents = amplifiedWithUnknownCN
                    ) else
                    EvaluationFactory.warn(
                        "$gene is amplified but undetermined if gene has a copy number >$requestedMinCopyNumber",
                        inclusionEvents = amplifiedWithUnknownCN
                    )
            }

            else -> evaluatePotentialWarns(evaluatedCopyNumbers, eventsOnNonCanonical, test.evidenceSource)
                ?: EvaluationFactory.fail("Insufficient copies of $gene")
        }
    }

    private fun evaluatePotentialWarns(
        evaluatedCopyNumbers: Map<CopyNumberEvaluation, Set<String>>,
        eventsOnNonCanonical: Set<String>,
        evidenceSource: String
    ): Evaluation? {
        val eventGroupsWithMessages = listOf(
            EventsWithMessages(
                evaluatedCopyNumbers[CopyNumberEvaluation.SUFFICIENT_COPY_NUMBER_WITH_LOSS_OF_FUNCTION],
                "$gene has sufficient copies but gene associated with loss-of-function protein impact in $evidenceSource"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[CopyNumberEvaluation.SUFFICIENT_COPY_NUMBER_ON_NON_ONCOGENE],
                "$gene has sufficient copies but gene known as TSG in $evidenceSource"
            ),
            EventsWithMessages(
                eventsOnNonCanonical,
                "$gene has sufficient copies but only on non-canonical transcript"
            )
        )

        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(eventGroupsWithMessages)
    }
}