package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.molecular.util.GeneConstants

private const val PLOIDY_AMPLIFICATION_FACTOR = 3.0
private const val ASSUMED_PLOIDY = 2.0
private const val ASSUMED_AMP_MIN_COPY_NR = 6

private enum class AmplificationEvaluation {
    ELIGIBLE_FULL_AMP,
    FULL_AMP_ON_TSG,
    FULL_AMP_WITH_LOSS_OF_FUNCTION,
    PARTIAL_AMP,
    NON_CANONICAL_AMP,
    NON_AMP_BUT_COPY_NR_MEETS_AMPLIFICATION_CUTOFF,
    NON_AMP_BUT_COPY_NR_MEETS_REQUESTED_COPY_NUMBER,
    FULL_AMP_WITH_UNKNOWN_COPY_NUMBER,
    PARTIAL_AMP_WITH_UNKNOWN_COPY_NUMBER,
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
            val thresholdNotRequestedOrNonCanonicalMinCopiesKnownAndMeetingThreshold =
                requestedMinCopyNumber == null || copyNumber.otherImpacts.any { it -> it.minCopies?.let { it >= requestedMinCopyNumber } == true }
            val thresholdNotRequestedAndMinCopiesKnownAndMeetingGeneralAmpThreshold =
                requestedMinCopyNumber == null && copyNumber.canonicalImpact.minCopies?.let { it > (PLOIDY_AMPLIFICATION_FACTOR * ploidy) } == true
            val thresholdRequestedAndMinCopiesKnownAndMeetingThreshold =
                requestedMinCopyNumber != null && copyNumber.canonicalImpact.minCopies?.let { it >= requestedMinCopyNumber } == true
            val hasUnknownCopyNumber = copyNumber.canonicalImpact.minCopies == null && copyNumber.otherImpacts.all { it.minCopies == null }

            return when {
                copyNumber.canonicalImpact.type == CopyNumberType.FULL_GAIN &&
                        thresholdNotRequestedOrMinCopiesKnownAndMeetingThreshold -> {
                    when {
                        copyNumber.geneRole == GeneRole.TSG -> FULL_AMP_ON_TSG

                        copyNumber.proteinEffect == ProteinEffect.LOSS_OF_FUNCTION ||
                                copyNumber.proteinEffect == ProteinEffect.LOSS_OF_FUNCTION_PREDICTED -> FULL_AMP_WITH_LOSS_OF_FUNCTION

                        else -> ELIGIBLE_FULL_AMP
                    }
                }

                copyNumber.canonicalImpact.type == CopyNumberType.PARTIAL_GAIN &&
                        thresholdNotRequestedOrMaxCopiesKnownAndMeetingThreshold -> PARTIAL_AMP

                !copyNumber.canonicalImpact.type.isGain && copyNumber.otherImpacts.any { it.type.isGain } &&
                        thresholdNotRequestedOrNonCanonicalMinCopiesKnownAndMeetingThreshold -> NON_CANONICAL_AMP

                thresholdNotRequestedAndMinCopiesKnownAndMeetingGeneralAmpThreshold -> NON_AMP_BUT_COPY_NR_MEETS_AMPLIFICATION_CUTOFF

                thresholdRequestedAndMinCopiesKnownAndMeetingThreshold -> NON_AMP_BUT_COPY_NR_MEETS_REQUESTED_COPY_NUMBER

                (copyNumber.canonicalImpact.type == CopyNumberType.FULL_GAIN || copyNumber.otherImpacts.any { it.type == CopyNumberType.FULL_GAIN }) &&
                        hasUnknownCopyNumber -> FULL_AMP_WITH_UNKNOWN_COPY_NUMBER

                (copyNumber.canonicalImpact.type == CopyNumberType.PARTIAL_GAIN || copyNumber.otherImpacts.any { it.type == CopyNumberType.PARTIAL_GAIN }) &&
                        hasUnknownCopyNumber -> PARTIAL_AMP_WITH_UNKNOWN_COPY_NUMBER

                else -> INELIGIBLE_COPY_NUMBER
            }
        }
    }
}

class GeneIsAmplified(override val gene: String, private val requestedMinCopyNumber: Int?) :
    MolecularEvaluationFunction(targetCoveragePredicate = specific(MolecularTestTarget.AMPLIFICATION, "Amplification of")) {

    override fun evaluate(test: MolecularTest, ihcTests: List<IhcTest>): Evaluation {
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

        val eligibleAmplification = evaluatedCopyNumbers[AmplificationEvaluation.ELIGIBLE_FULL_AMP]
        val fullAmplificationWithUnknownCopyNumber = evaluatedCopyNumbers[AmplificationEvaluation.FULL_AMP_WITH_UNKNOWN_COPY_NUMBER]
        val requestedCopiesMessage = requestedMinCopyNumber?.let { " with >= $requestedMinCopyNumber copies" } ?: ""

        val ihcTestEvaluation =
            if (gene in GeneConstants.IHC_AMP_EVALUABLE_GENES_TO_PROTEINS.keys) IhcTestEvaluation.create(
                GeneConstants.IHC_AMP_EVALUABLE_GENES_TO_PROTEINS.getValue(
                    gene
                ), ihcTests
            ) else null
        val hasPositiveIhcEvaluation = ihcTestEvaluation?.hasCertainBroadPositiveResultsForItem() == true

        return when {
            eligibleAmplification != null -> {
                EvaluationFactory.pass("$gene is amplified$requestedCopiesMessage", inclusionEvents = eligibleAmplification)
            }

            fullAmplificationWithUnknownCopyNumber != null -> {
                when {
                    requestedMinCopyNumber == null ->
                        EvaluationFactory.pass("$gene is amplified", inclusionEvents = fullAmplificationWithUnknownCopyNumber)

                    requestedMinCopyNumber <= ASSUMED_AMP_MIN_COPY_NR ->
                        EvaluationFactory.pass(
                            "$gene is amplified hence assumed gene is amplified$requestedCopiesMessage",
                            inclusionEvents = fullAmplificationWithUnknownCopyNumber
                        )

                    else ->
                        EvaluationFactory.warn(
                            "$gene is amplified but undetermined if$requestedCopiesMessage",
                            inclusionEvents = fullAmplificationWithUnknownCopyNumber
                        )
                }
            }

            else -> evaluatePotentialOtherWarns(evaluatedCopyNumbers, test.evidenceSource, requestedCopiesMessage, hasPositiveIhcEvaluation)
                ?: EvaluationFactory.fail("No amplification of $gene$requestedCopiesMessage")
        }
    }

    private fun evaluatePotentialOtherWarns(
        evaluatedCopyNumbers: Map<AmplificationEvaluation, Set<String>>,
        evidenceSource: String,
        requestedCopiesMessage: String,
        hasPositiveIhcEvaluation: Boolean
    ): Evaluation? {
        val eventGroupsWithMessages = listOf(
            EventsWithMessages(
                evaluatedCopyNumbers[AmplificationEvaluation.FULL_AMP_WITH_LOSS_OF_FUNCTION],
                "$gene is amplified$requestedCopiesMessage but gene associated with loss-of-function protein impact in $evidenceSource",
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[AmplificationEvaluation.FULL_AMP_ON_TSG],
                "$gene is amplified$requestedCopiesMessage but gene known as TSG in $evidenceSource"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[AmplificationEvaluation.PARTIAL_AMP],
                "$gene is amplified$requestedCopiesMessage but only partially"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[AmplificationEvaluation.NON_CANONICAL_AMP],
                "$gene is amplified$requestedCopiesMessage but on non-canonical transcript"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[AmplificationEvaluation.NON_AMP_BUT_COPY_NR_MEETS_AMPLIFICATION_CUTOFF],
                "$gene is not annotated as amp but meets amplification threshold of $PLOIDY_AMPLIFICATION_FACTOR * (assumed) ploidy"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[AmplificationEvaluation.NON_AMP_BUT_COPY_NR_MEETS_REQUESTED_COPY_NUMBER],
                "$gene is not annotated as amp but meets requested copy nr of >= $requestedMinCopyNumber copies"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[AmplificationEvaluation.PARTIAL_AMP_WITH_UNKNOWN_COPY_NUMBER],
                "$gene is amplified but partially and undetermined if copy nr meets threshold of >= $requestedMinCopyNumber copies"
            ),
        )

        val finalEventGroupsWithMessages = if (hasPositiveIhcEvaluation) eventGroupsWithMessages +
                EventsWithMessages(
                    setOf("Possible $gene amp"),
                    "$gene may be amplified$requestedCopiesMessage - based on positive ${
                        GeneConstants.IHC_AMP_EVALUABLE_GENES_TO_PROTEINS.getValue(
                            gene
                        )
                    } IHC result"
                ) else eventGroupsWithMessages

        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(finalEventGroupsWithMessages)
    }
}