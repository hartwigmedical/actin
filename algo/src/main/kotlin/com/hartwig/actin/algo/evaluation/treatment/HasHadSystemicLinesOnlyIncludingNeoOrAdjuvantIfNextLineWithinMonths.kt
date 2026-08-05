package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.treatmentHistoryEntryIsSystemic
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import java.time.LocalDate

class HasHadSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonths(
    private val referenceTreatmentCount: Int,
    private val maxMonthsBeforeNextLine: Int,
    private val referenceDate: LocalDate,
    private val comparator: (Int, Int) -> Boolean,
    private val comparatorMessage: String,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val timingEvaluatedHistory = SystemicTreatmentAnalyser.evaluateTreatmentTimingRelativeToNextLine(
            record.oncologicalHistory.filter(::treatmentHistoryEntryIsSystemic),
            maxMonthsBeforeNextLine,
            referenceDate
        )

        val (certainlyCountingEntries, potentiallyCountingCurativeAndNeoAdjuvantEntries) = timingEvaluatedHistory.partition {
            val passOnIntent = it.entry.intents?.intersect(Intent.curativeAdjuvantNeoadjuvantSet()).isNullOrEmpty()
            passOnIntent || it.timing == SystemicTreatmentAnalyser.TreatmentTiming.WITHIN
        }

        val curativeAdjuvantOrNeoadjuvantEntriesWithAmbiguousTiming = potentiallyCountingCurativeAndNeoAdjuvantEntries.filter {
            it.timing in setOf(SystemicTreatmentAnalyser.TreatmentTiming.AMBIGUOUS, SystemicTreatmentAnalyser.TreatmentTiming.UNKNOWN)
        }

        val minCertainCount = SystemicTreatmentAnalyser.minSystemicTreatments(certainlyCountingEntries.map { it.entry })
        val maxPotentialCount = SystemicTreatmentAnalyser.maxSystemicTreatments(
            (certainlyCountingEntries + curativeAdjuvantOrNeoadjuvantEntriesWithAmbiguousTiming).map { it.entry }
        )

        return when {
            comparator(minCertainCount, referenceTreatmentCount) ->
                EvaluationFactory.pass(
                    labels.hasHadSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonthsPass(comparatorMessage, referenceTreatmentCount)
                )

            comparator(maxPotentialCount, referenceTreatmentCount) -> {
                val undeterminedMessageEnding = curativeAdjuvantOrNeoadjuvantEntriesWithAmbiguousTiming.takeIf { it.isNotEmpty() }
                    ?.let {
                        labels.hasHadSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonthsUndeterminedEnding(maxMonthsBeforeNextLine)
                    } ?: ""
                EvaluationFactory.undetermined(
                    labels.hasHadSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonthsUndetermined(
                        comparatorMessage, referenceTreatmentCount, undeterminedMessageEnding
                    )
                )
            }

            else -> EvaluationFactory.fail(
                labels.hasHadSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonthsFail(comparatorMessage, referenceTreatmentCount)
            )
        }
    }

    companion object {
        fun createForMinimumTreatmentLines(
            referenceTreatmentCount: Int,
            maxMonthsBeforeNextLine: Int,
            referenceDate: LocalDate,
            labels: EvaluationLabels.Treatment
        ): EvaluationFunction {
            return HasHadSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonths(
                referenceTreatmentCount,
                maxMonthsBeforeNextLine,
                referenceDate,
                { count, reference -> count >= reference },
                "least",
                labels
            )
        }

        fun createForMaximumTreatmentLines(
            referenceTreatmentCount: Int,
            maxMonthsBeforeNextLine: Int,
            referenceDate: LocalDate,
            labels: EvaluationLabels.Treatment
        ): EvaluationFunction {
            return HasHadSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonths(
                referenceTreatmentCount,
                maxMonthsBeforeNextLine,
                referenceDate,
                { count, reference -> count <= reference },
                "most",
                labels
            )
        }
    }
}