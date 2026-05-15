package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
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
    private val comparatorMessage: String
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
                EvaluationFactory.pass("Received at $comparatorMessage $referenceTreatmentCount systemic treatments")


            comparator(maxPotentialCount, referenceTreatmentCount) -> {
                val undeterminedMessageEnding = curativeAdjuvantOrNeoadjuvantEntriesWithAmbiguousTiming.takeIf { it.isNotEmpty() }
                    ?.let {
                        " since it is unclear if (neo)adjuvant treatment(s) resulted in PD within $maxMonthsBeforeNextLine months after " +
                                "stopping (incomplete date information)"
                    } ?: ""
                EvaluationFactory.undetermined(
                    "Undetermined if received at $comparatorMessage $referenceTreatmentCount systemic treatments$undeterminedMessageEnding"
                )
            }

            else -> EvaluationFactory.fail("Has not received at $comparatorMessage $referenceTreatmentCount systemic treatments")
        }
    }

    companion object {
        fun createForMinimumTreatmentLines(
            referenceTreatmentCount: Int,
            maxMonthsBeforeNextLine: Int,
            referenceDate: LocalDate
        ): EvaluationFunction {
            return HasHadSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonths(
                referenceTreatmentCount,
                maxMonthsBeforeNextLine,
                referenceDate,
                { count, reference -> count >= reference },
                "least"
            )
        }

        fun createForMaximumTreatmentLines(
            referenceTreatmentCount: Int,
            maxMonthsBeforeNextLine: Int,
            referenceDate: LocalDate
        ): EvaluationFunction {
            return HasHadSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonths(
                referenceTreatmentCount,
                maxMonthsBeforeNextLine,
                referenceDate,
                { count, reference -> count <= reference },
                "most"
            )
        }
    }
}