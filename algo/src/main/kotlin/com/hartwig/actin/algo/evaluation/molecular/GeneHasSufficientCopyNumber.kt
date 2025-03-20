package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import java.time.LocalDate

private enum class CopyNumberEvaluation2 {
    REPORTABLE_SUFFICIENT_COPY_NUMBER,
    SUFFICIENT_COPY_NUMBER_WITH_LOSS_OF_FUNCTION,
    SUFFICIENT_COPY_NUMBER_ON_NON_ONCOGENE,
    UNREPORTABLE_SUFFICIENT_COPY_NUMBER;

    companion object {
        fun fromCopyNumber(copyNumber: CopyNumber): CopyNumberEvaluation2 {
            return when {
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
            }
        }
}

class GeneHasSufficientCopyNumber(private val gene: String, private val requestedMinCopyNumber: Int, maxTestAge: LocalDate? = null) :
    MolecularEvaluationFunction(maxTestAge) {

    override fun genes() = listOf(gene)

    override fun evaluate(test: MolecularTest): Evaluation {
        val evaluatedCopyNumbers: Map<CopyNumberEvaluation2, Set<String>> = test.drivers.copyNumbers.filter { copyNumber ->
            copyNumber.gene == gene && (copyNumber.canonicalImpact.minCopies >= requestedMinCopyNumber)
        }
            .groupBy({ copyNumber ->
                CopyNumberEvaluation2.fromCopyNumber(copyNumber)
            }, valueTransform = CopyNumber::event)
            .mapValues { (_, copyNumberEvents) -> copyNumberEvents.toSet() }

        val eventsOnNonCanonical = test.drivers.copyNumbers.filter { copyNumber ->
            copyNumber.gene == gene && copyNumber.otherImpacts.any { it.minCopies >= requestedMinCopyNumber }
        }.map { it.event }.toSet()

        return evaluatedCopyNumbers[CopyNumberEvaluation2.REPORTABLE_SUFFICIENT_COPY_NUMBER]?.let { reportableSufficientCN ->
            EvaluationFactory.pass("$gene copy number is above requested $requestedMinCopyNumber", inclusionEvents = reportableSufficientCN)
        }
            ?: evaluatedCopyNumbers[CopyNumberEvaluation2.UNREPORTABLE_SUFFICIENT_COPY_NUMBER]?.let { unreportableSufficientCN ->
                EvaluationFactory.pass(
                    "$gene has a copy number >$requestedMinCopyNumber copies but is considered not reportable",
                    inclusionEvents = unreportableSufficientCN
                )
            }
            ?: evaluatePotentialWarns(evaluatedCopyNumbers, eventsOnNonCanonical, test.evidenceSource)
            ?: EvaluationFactory.fail("Insufficient copies of $gene")
    }

    private fun evaluatePotentialWarns(
        evaluatedCopyNumbers: Map<CopyNumberEvaluation2, Set<String>>,
        eventsOnNonCanonical: Set<String>,
        evidenceSource: String
    ): Evaluation? {
        val eventGroupsWithMessages = listOf(
            EventsWithMessages(
                evaluatedCopyNumbers[CopyNumberEvaluation2.SUFFICIENT_COPY_NUMBER_WITH_LOSS_OF_FUNCTION],
                "$gene has sufficient copies but gene associated with loss-of-function protein impact in $evidenceSource"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[CopyNumberEvaluation2.SUFFICIENT_COPY_NUMBER_ON_NON_ONCOGENE],
                "$gene has sufficient copies but gene known as TSG in $evidenceSource"
            ),
            EventsWithMessages(
                evaluatedCopyNumbers[CopyNumberEvaluation2.UNREPORTABLE_SUFFICIENT_COPY_NUMBER],
                "$gene has sufficient copies but not considered reportable"
            ),
            EventsWithMessages(
                eventsOnNonCanonical,
                "$gene has sufficient copies but only on non-canonical transcript"
            )
        )

        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(eventGroupsWithMessages)
    }
}