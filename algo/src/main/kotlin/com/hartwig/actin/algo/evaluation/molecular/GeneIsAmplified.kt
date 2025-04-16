package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import java.time.LocalDate

private const val PLOIDY_FACTOR = 3.0

private enum class AmplificationEvaluation {
    REPORTABLE_FULL_AMP,
    REPORTABLE_PARTIAL_AMP,
    AMP_WITH_LOSS_OF_FUNCTION,
    AMP_ON_NON_ONCOGENE,
    UNREPORTABLE_AMP,
    NON_AMP_WITH_SUFFICIENT_COPY_NUMBER;

    companion object {
        fun fromCopyNumber(copyNumber: CopyNumber, relativeMinCopies: Double, relativeMaxCopies: Double): AmplificationEvaluation {
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
    override fun targetsRequiredPredicate() = TargetPredicate.exactly(MolecularTestTarget.AMPLIFICATION)

    override fun evaluate(test: MolecularTest): Evaluation {
        val ploidy = test.characteristics.ploidy ?: return EvaluationFactory.fail("Amplification for $gene undetermined (ploidy missing)")

        val evaluatedCopyNumbers: Map<AmplificationEvaluation, Set<String>> = test.drivers.copyNumbers.filter { copyNumber ->
            copyNumber.gene == gene && (requestedMinCopyNumber == null || copyNumber.canonicalImpact.minCopies >= requestedMinCopyNumber)
        }
            .groupBy({ copyNumber ->
                AmplificationEvaluation.fromCopyNumber(
                    copyNumber, copyNumber.canonicalImpact.minCopies / ploidy,
                    copyNumber.canonicalImpact.maxCopies / ploidy
                )
            }, valueTransform = CopyNumber::event)
            .mapValues { (_, copyNumberEvents) -> copyNumberEvents.toSet() }

        val eventsOnNonCanonical = test.drivers.copyNumbers.filter { copyNumber ->
            copyNumber.gene == gene && (requestedMinCopyNumber != null && copyNumber.otherImpacts.any { it.minCopies >= requestedMinCopyNumber })
        }.map { it.event }.toSet()

        val minCopyMessage = requestedMinCopyNumber?.let { " with >=$requestedMinCopyNumber copies" } ?: ""
        return evaluatedCopyNumbers[AmplificationEvaluation.REPORTABLE_FULL_AMP]?.let { reportableFullAmps ->
            EvaluationFactory.pass("$gene is amplified$minCopyMessage", inclusionEvents = reportableFullAmps)
        }
            ?: requestedMinCopyNumber?.let { evaluatedCopyNumbers[AmplificationEvaluation.UNREPORTABLE_AMP] }
                ?.let { ampsThatAreUnreportable ->
                    EvaluationFactory.pass(
                        "$gene has a copy number >$requestedMinCopyNumber copies but is considered not reportable",
                        inclusionEvents = ampsThatAreUnreportable
                    )
                }
            ?: evaluatePotentialWarns(evaluatedCopyNumbers, eventsOnNonCanonical, test.evidenceSource)
            ?: EvaluationFactory.fail(
                if (requestedMinCopyNumber == null) "No amplification of $gene$minCopyMessage" else "Insufficient copies of $gene"
            )
    }

    private fun evaluatePotentialWarns(
        evaluatedCopyNumbers: Map<AmplificationEvaluation, Set<String>>,
        eventsOnNonCanonical: Set<String>,
        evidenceSource: String
    ): Evaluation? {
        val eventGroupsWithMessages = listOf(
            EventsWithMessages(evaluatedCopyNumbers[AmplificationEvaluation.REPORTABLE_PARTIAL_AMP], "$gene partially amplified"),
            EventsWithMessages(
                evaluatedCopyNumbers[AmplificationEvaluation.AMP_WITH_LOSS_OF_FUNCTION],
                "$gene is amplified but gene associated with loss-of-function protein impact in $evidenceSource"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[AmplificationEvaluation.AMP_ON_NON_ONCOGENE],
                "$gene is amplified but gene known as TSG in $evidenceSource"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[AmplificationEvaluation.UNREPORTABLE_AMP],
                "$gene is amplified but not considered reportable"
            ),
            EventsWithMessages(
                requestedMinCopyNumber?.let { evaluatedCopyNumbers[AmplificationEvaluation.NON_AMP_WITH_SUFFICIENT_COPY_NUMBER] },
                "$gene does not meet cut-off for amplification but has sufficient copy number > $requestedMinCopyNumber"
            ),
            EventsWithMessages(
                eventsOnNonCanonical,
                "Gene $gene is (partially) amplified or has sufficient copies but only on non-canonical transcript"
            )
        )

        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(eventGroupsWithMessages)
    }
}