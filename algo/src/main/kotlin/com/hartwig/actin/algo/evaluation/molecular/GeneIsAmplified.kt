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

private const val PLOIDY_AMPLIFICATION_FACTOR = 3.0
private const val ASSUMED_PLOIDY = 2.0
private const val ASSUMED_MIN_COPY_NR_AMP = 6

private enum class AmplificationEvaluation {
    ELIGIBLE_FULL_AMP,
    FULL_AMP_ON_NON_ONCOGENE,
    FULL_AMP_WITH_LOSS_OF_FUNCTION,
    PARTIAL_AMP,
    NON_CANONICAL_AMP,
    NON_AMP_BUT_SUFFICIENT_COPY_NUMBER,
    AMP_WITH_UNKNOWN_COPY_NUMBER,
    INELIGIBLE_COPY_NUMBER;

    companion object {
        fun fromCopyNumber(
            copyNumber: CopyNumber,
            requestedMinCopyNumber: Int?,
            ploidy: Double
        ): AmplificationEvaluation {
            val thresholdNotRequestedOrMinCopiesKnownAndMeetingThreshold =
                requestedMinCopyNumber == null || copyNumber.canonicalImpact.minCopies?.let { it >= requestedMinCopyNumber } == true
            val thresholdNotRequestedOrMaxCopiesKnownAndMeetingThreshold =
                requestedMinCopyNumber == null || copyNumber.canonicalImpact.maxCopies?.let { it >= requestedMinCopyNumber } == true
            val thresholdNotRequestedAndMinCopiesKnownAndMeetingGeneralAmpThreshold =
                requestedMinCopyNumber == null && copyNumber.canonicalImpact.minCopies?.let { copies -> copies > PLOIDY_AMPLIFICATION_FACTOR * ploidy } == true
            val thresholdNotRequestedOrNonCanonicalMinCopiesKnownAndMeetingThreshold =
                requestedMinCopyNumber == null || copyNumber.otherImpacts.any { it.minCopies?.let { it >= requestedMinCopyNumber } == true }
            val thresholdRequestedAndMinCopiesKnownAndMeetingThreshold =
                requestedMinCopyNumber != null && copyNumber.canonicalImpact.minCopies?.let { it >= requestedMinCopyNumber } == true

            return when {
                copyNumber.canonicalImpact.type == CopyNumberType.FULL_GAIN &&
                        thresholdNotRequestedOrMinCopiesKnownAndMeetingThreshold -> {
                    when {
                        copyNumber.geneRole == GeneRole.TSG -> FULL_AMP_ON_NON_ONCOGENE

                        copyNumber.proteinEffect == ProteinEffect.LOSS_OF_FUNCTION ||
                                copyNumber.proteinEffect == ProteinEffect.LOSS_OF_FUNCTION_PREDICTED -> FULL_AMP_WITH_LOSS_OF_FUNCTION

                        else -> ELIGIBLE_FULL_AMP
                    }
                }

                (copyNumber.canonicalImpact.type.isGain || copyNumber.otherImpacts.any { it.type.isGain }) &&
                        copyNumber.canonicalImpact.minCopies == null && copyNumber.otherImpacts.all { it.minCopies == null } -> AMP_WITH_UNKNOWN_COPY_NUMBER

                copyNumber.canonicalImpact.type == CopyNumberType.PARTIAL_GAIN &&
                        thresholdNotRequestedOrMaxCopiesKnownAndMeetingThreshold -> PARTIAL_AMP

                !copyNumber.canonicalImpact.type.isGain && copyNumber.otherImpacts.any { it.type.isGain } &&
                        thresholdNotRequestedOrNonCanonicalMinCopiesKnownAndMeetingThreshold -> NON_CANONICAL_AMP

                thresholdNotRequestedAndMinCopiesKnownAndMeetingGeneralAmpThreshold ||
                        thresholdRequestedAndMinCopiesKnownAndMeetingThreshold -> NON_AMP_BUT_SUFFICIENT_COPY_NUMBER

                else -> INELIGIBLE_COPY_NUMBER
            }
        }
    }
}

class GeneIsAmplified(override val gene: String, private val requestedMinCopyNumber: Int?, maxTestAge: LocalDate? = null) :
    MolecularEvaluationFunction(
        targetCoveragePredicate = specific(MolecularTestTarget.AMPLIFICATION, "Amplification of"),
        maxTestAge = maxTestAge
    ) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val evaluatedCopyNumbers: Map<AmplificationEvaluation, Set<String>> =
            test.drivers.copyNumbers.filter { copyNumber -> copyNumber.gene == gene }
                .groupBy({ copyNumber ->
                    AmplificationEvaluation.fromCopyNumber(
                        copyNumber,
                        requestedMinCopyNumber,
                        test.characteristics.ploidy ?: ASSUMED_PLOIDY
                    )
                }, valueTransform = CopyNumber::event)
                .mapValues { (_, copyNumberEvents) -> copyNumberEvents.toSet() }

        val eligibleFullAmps = evaluatedCopyNumbers[AmplificationEvaluation.ELIGIBLE_FULL_AMP]
        val ampsWithUnknownCopyNumber = evaluatedCopyNumbers[AmplificationEvaluation.AMP_WITH_UNKNOWN_COPY_NUMBER]

        val minCopyMessage = requestedMinCopyNumber?.let { " with >=$requestedMinCopyNumber copies" } ?: ""
        return when {
            eligibleFullAmps != null -> {
                EvaluationFactory.pass("$gene is amplified$minCopyMessage", inclusionEvents = eligibleFullAmps)
            }

            ampsWithUnknownCopyNumber != null -> {
                when {
                    requestedMinCopyNumber == null ->
                        EvaluationFactory.pass("$gene is amplified", inclusionEvents = ampsWithUnknownCopyNumber)

                    requestedMinCopyNumber <= ASSUMED_MIN_COPY_NR_AMP ->
                        EvaluationFactory.pass(
                            "$gene is amplified hence assumed gene has a copy number >$requestedMinCopyNumber",
                            inclusionEvents = ampsWithUnknownCopyNumber
                        )

                    else ->
                        EvaluationFactory.warn(
                            "$gene is amplified but undetermined if$minCopyMessage",
                            inclusionEvents = ampsWithUnknownCopyNumber
                        )
                }
            }

            else -> evaluatePotentialWarns(evaluatedCopyNumbers, test.evidenceSource)
                ?: EvaluationFactory.fail("No amplification of $gene$minCopyMessage")
        }
    }

    private fun evaluatePotentialWarns(
        evaluatedCopyNumbers: Map<AmplificationEvaluation, Set<String>>,
        evidenceSource: String
    ): Evaluation? {
        val eventGroupsWithMessages = listOf(
            EventsWithMessages(
                evaluatedCopyNumbers[AmplificationEvaluation.FULL_AMP_WITH_LOSS_OF_FUNCTION],
                "$gene is amplified but gene associated with loss-of-function protein impact in $evidenceSource",
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[AmplificationEvaluation.FULL_AMP_ON_NON_ONCOGENE],
                "$gene is amplified but gene known as TSG in $evidenceSource"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[AmplificationEvaluation.PARTIAL_AMP],
                "$gene is amplified but partial"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[AmplificationEvaluation.NON_CANONICAL_AMP],
                "$gene is amplified but on non-canonical transcript"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[AmplificationEvaluation.NON_AMP_BUT_SUFFICIENT_COPY_NUMBER],
                if (requestedMinCopyNumber == null) "$gene is not annotated as amp but meets amplification cutoff" else "$gene is not annotated as amp but meets required copy nr of $requestedMinCopyNumber copies"
            ),
        )

        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(eventGroupsWithMessages)
    }
}